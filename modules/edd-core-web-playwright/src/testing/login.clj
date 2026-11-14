(ns testing.login
  "Cognito-backed UI login bypass for Playwright integration tests.

   The standard SPA login flow takes ~5–10s per test (form fill + Cognito
   round-trip + token exchange + JS app reinit). For a 10-test suite that's
   over a minute of pure overhead.

   This namespace replaces it with:

     1. Call Cognito `AdminInitiateAuth` from the JVM (1 HTTPS round-trip).
     2. Seed `localStorage.auth` on the Playwright page with the resulting
        token bundle, in the exact shape the SPA writes after a successful
        login (see edd-core-web login widget core.cljs:181-187).
     3. Navigate to the SPA. The login widget's startup code reads
        `localStorage.auth` and dispatches `::login-succeeded` if a non-empty
        `:id-token` is found, so the SPA is fully authenticated by the time
        the first page renders — no login form ever appears.

   This is a pure bypass: tokens are real Cognito tokens, every backend
   call uses normal JWT auth. Only the UI form is skipped.

   Requires:
   - The SPA must be using a version of edd-core-web's login widget that
     restores from localStorage on init (web/widgets/login/core.cljs/init
     calling restore-stored-auth!).
   - AWS credentials available to the JVM (env vars / SSO / instance profile).
   - The IAM principal must have cognito-idp:AdminInitiateAuth on the pool.
   - `ADMIN_NO_SRP_AUTH` enabled on the user-pool client (samurai's is)."
  (:require [clojure.data.json :as json]
            [testing.pw :refer [*current-page* step]])
  (:import [software.amazon.awssdk.auth.credentials DefaultCredentialsProvider]
           [software.amazon.awssdk.regions Region]
           [software.amazon.awssdk.services.cognitoidentityprovider
            CognitoIdentityProviderClient]
           [software.amazon.awssdk.services.cognitoidentityprovider.model
            AdminInitiateAuthRequest
            AuthFlowType]))

(defn- cognito-client
  "Build a CognitoIdentityProviderClient for `region`. The client is cheap
   to construct (each call sets up a new HTTP client under the hood) so we
   build one per `cognito-tokens` invocation rather than caching."
  [region]
  (-> (CognitoIdentityProviderClient/builder)
      (.region (Region/of region))
      (.credentialsProvider (DefaultCredentialsProvider/create))
      (.build)))

(defn cognito-tokens
  "Authenticate `username`/`password` against the given Cognito user-pool
   via ADMIN_NO_SRP_AUTH and return a map shaped exactly like the SPA's
   `localStorage.auth` blob:

     {:id-token \"...\" :access-token \"...\" :refresh-token \"...\"}

   Throws if authentication fails or if Cognito returns a challenge (e.g.
   NEW_PASSWORD_REQUIRED) — challenges are not handled here because the
   admin user used by tests must be in a fully-confirmed state.

   Required keys in `cognito-config`:
     :region              AWS region of the user-pool, e.g. \"eu-central-1\"
     :user-pool-id        e.g. \"eu-central-1_aBcDeFgHi\"
     :user-pool-client-id Cognito app client id

   Pure JVM call — no Playwright involvement."
  [{:keys [region user-pool-id user-pool-client-id] :as _cognito-config}
   username password]
  (with-open [client (cognito-client region)]
    (let [req
          (-> (AdminInitiateAuthRequest/builder)
              (.userPoolId user-pool-id)
              (.clientId user-pool-client-id)
              (.authFlow AuthFlowType/ADMIN_NO_SRP_AUTH)
              (.authParameters {"USERNAME" username
                                "PASSWORD" password})
              (.build))

          resp
          (.adminInitiateAuth client req)

          challenge
          (.challengeNameAsString resp)]
      (when challenge
        (throw (ex-info (str "Cognito returned a challenge instead of tokens: " challenge)
                        {:challenge challenge :username username})))
      (let [auth-result (.authenticationResult resp)]
        {:id-token      (.idToken auth-result)
         :access-token  (.accessToken auth-result)
         :refresh-token (.refreshToken auth-result)}))))

(defn- seed-auth-storage!
  "Inject `tokens` into `localStorage.auth` on `page` using the exact
   serialization the SPA produces after login: keys are kebab-case strings
   (`id-token` / `access-token` / `refresh-token`), value is JSON.

   Must be called AFTER an initial navigate, because localStorage is
   origin-scoped — a brand-new BrowserContext has no document loaded and
   no origin to bind storage to."
  [page tokens]
  (let [;; Match clj->js conversion the SPA uses: keywords -> their name.
        js-shape
        {"id-token"      (:id-token tokens)
         "access-token"  (:access-token tokens)
         "refresh-token" (:refresh-token tokens)}

        js-arg
        (json/write-str js-shape)]
    ;; addInitScript runs on every navigation/reload BEFORE any page
    ;; script, so the SPA's startup reads the seeded value on first
    ;; paint. JSON.parse is needed because addInitScript serializes the
    ;; arg via JSON anyway, and we want the localStorage value to be a
    ;; JSON string (not a stringified-twice JSON string).
    (.addInitScript
     (.context page)
     (str "(() => {"
          "  try { window.localStorage.setItem('auth', " (json/write-str js-arg) "); }"
          "  catch (e) { /* storage not yet available, init script will rerun */ }"
          "})();"))))

(defn login!
  "End-to-end UI login bypass: authenticate against Cognito, seed
   `localStorage.auth` on the active page, and navigate to `base-url`.
   On return, the SPA is fully logged in and the first page has loaded.

   Reads the page from `testing.pw/*current-page*`, so call this from
   inside `with-ui` exactly like every other helper.

   `cognito-config` shape — see `cognito-tokens`.

   Wrapped in a `step` so the trace shows a single collapsible
   'Cognito login' entry instead of an opaque pause."
  [base-url cognito-config username password]
  (step (str "Cognito login bypass for " username " (no UI form)")
        (fn []
          (let [tokens
                (cognito-tokens cognito-config username password)]
            (seed-auth-storage! *current-page* tokens)
            (.navigate *current-page* base-url)))))
