# Migration

## Login widget

Events, subs, views and effects keep their names. What changes for an app:

- English texts: "Log in" (was "Login"), "Create an account" ("Register"),
  "Email address" ("Username (e-mail)"), "Log out" ("Logout"). Tests that find
  the dialog by text need the new words, or keep the old ones:

  ```clojure
  (rf/dispatch [::login-events/init
                {:translations {:en {:login    "Login"
                                     :register "Register"
                                     :username "Username (e-mail)"
                                     :logout   "Logout"
                                     :titles   {:login    "Login"
                                                :register "Register"}}}}])
  ```

- The dialog is a `<form>`: Enter sends it and the submit button is disabled
  only while a request runs. An incomplete form says what is missing.
- `::db/username` holds the email as typed; effects trim and lower-case it
  (`utils/sent-email`) when they call Cognito.
- Errors are always translation keys, never Cognito's sentence
  (`utils/cognito-error-key`).
- The password reset moves to the code step only after Cognito answered, with
  the same notice for an address with and without an account. Its "Resend
  code" calls `ForgotPassword`.
- The email that logged in last is kept in local storage
  (`web.widgets.login/last-email`) and filled in when the dialog opens.
  Switch it off with `{:remember-email false}`.
- The password toggle switches the field to `type="text"` (was `"input"`).

New `::init` options: `:remember-email` (true), `:register-note` (none),
`:translations` (none), `:focus-fallback` (none).

## `:classes` removed

`:classes` was left over from MUI v4 `withStyles`. Nothing filled it, so every
`(:x classes)` was `nil` and no style behind it was ever applied. It is gone:

- Page panels are called with no arguments.
- `body` in `web.widgets.page-frame.page` and `web.widgets.frame.page` renders
  the page directly; `menu-item` takes only the item.
- The template no longer has `styles.cljs` or a `:styles` key.

Your app looks the same after migrating, because none of these styles worked.
Existing panels with a `[classes]` argument keep working (it is `nil`), so the
steps below are cleanup.

### Steps

1. Drop the argument from page panels:

   ```clojure
   ;; before
   (defn main-panel
     [classes]
     ...)

   ;; after
   (defn main-panel
     []
     ...)
   ```

2. Delete every `:class-name (:x classes)` and `{:keys [classes]}`.

3. If your own `body` wraps the page in `r/reactify-component` to read
   `props.classes`, render the page directly:

   ```clojure
   (defn body
     [{:keys [theme] :as ctx}]
     [RawThemeProvider {:theme theme}
      [page ctx]])
   ```

4. Remove `:styles` from `core/init` and its namespace, unless you use
   `web.widgets.page-frame.page` (see below).

5. Style with `:sx` on the component, and put app-wide colors and typography
   in `:theme` (passed to MUI `createTheme`):

   ```clojure
   [RawButton {:sx {:ml "auto"}} "Save"]

   (core/init
    {...
     :theme {:palette {:primary {:main "#1976d2"}}}})
   ```

### `page-frame` `:styles`

`web.widgets.page-frame.page` still applies `:styles`: each key becomes a CSS
class selector under the page. Match it with a plain string class name:

```clojure
(defn styles
  [_theme]
  {:highlight {:color "green"}})

[:div {:class-name "highlight"} "..."]
```
