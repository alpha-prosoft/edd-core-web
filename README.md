# edd-core-web

Core library for building re-frame applications: routing, pages, i18n and
calls to edd services.

## Creating a project

Needs a JVM, the Clojure CLI and Node.js.

```sh
clojure -Sdeps '{:deps {com.alpha-prosoft/edd-core-web {:mvn/version "2.268.0"}}}' \
  -M -m edd.bootstrap create-project my-app my-app
cd my-app
clojure -M:shadow-cljs:dev:test:run
```

The arguments are the target directory and the project name. The app runs on
https://my-app.local.localdevhub.com:3000; its TLS certificate is downloaded
to `ext/` on first start. `clojure -M:format fix` formats the code and
`clojure -X:clean` removes build output.

The generated project uses edd-web-primitives and edd-web-widgets for UI
components, pinned to the same version as edd-core-web. Its `AGENTS.md`
describes how to use the libraries; `clojure -M:it` runs the Playwright tests,
`clojure -M:guide` regenerates the screenshot user guide in `docs/guide` and
`clojure -M:explore` opens an nREPL with a browser for exploring the app, all
against the running app. They are built on
[edd-core-web-playwright](modules/edd-core-web-playwright).

## Defining the app

`edd.core/init` wires pages, routes, menu, translations and theme:

```clojure
(ns my-app.core
  (:require [edd.core :as core]
            [my-app.home.core :as home]
            [my-app.about.core :as about]))

(defn ^:export init
  [& [_config]]
  (core/init
   {:pages        {:home  home/main
                   :about about/main}
    :routes       [["/" :home]
                   ["/about/:id" :about]]
    :menu         [:home :about]
    :languages    [:en :de]
    :translations translations
    :name         "my-app"}))
```

A page is a function returning the event to dispatch on entry and the view:

```clojure
(defn main
  [_ctx]
  {:init  ::events/init
   :panel views/main-panel})
```

Navigate between pages with:

```clojure
(rf/dispatch [::edd.events/navigate :about {:id id}])
```

Override the built-in error pages with
`:error-pages {:not-found my-404 :bad-request my-400}`.

## Calling services

`::edd.client/call` sends a query or commands to a service and dispatches
`:on-success` or `:on-failure` with the response appended:

```clojure
(rf/reg-event-fx
 ::load-user
 (fn [_ [_ id]]
   {::client/call {:service    :user-svc
                   :query      {:query-id :get-user
                                :id       id}
                   :on-success [::user-loaded]
                   :on-failure [::user-load-failed]}}))
```

Use `:command` for a single command or `:commands` for several, and
`:retry {:attempts 3}` to retry. `::client/call-n` takes `{:items [...]}` and
runs several calls in parallel.

### Dependent queries (`::client/deps`)

`::client/deps` resolves a graph of queries and dispatches `:on-success` once
with all results. Independent deps run in parallel; a dep with `:depends-on`
waits for its inputs and receives them in its `:query` function.

```clojure
(rf/reg-event-fx
 ::open-profile
 (fn [_ _]
   {::client/deps
    {:deps {:user    {:service :user-svc
                      :query   {:query-id :get-current-user}}

            :profile {:service    :user-svc
                      :depends-on [:user]
                      :query      (fn [{:keys [user]}]
                                    {:query-id :get-profile
                                     :user-id  (:id user)})}}
     :on-success [::profile-loaded]
     :on-failure [::profile-load-failed]}}))

(rf/reg-event-db
 ::profile-loaded
 (fn [db [_ {:keys [user profile]}]]
   (assoc db :user user :profile profile)))
```

| Key           | Required | Description                                                   |
| ------------- | -------- | ------------------------------------------------------------- |
| `:service`    | yes      | Target service, as for `::client/call`.                       |
| `:query`      | yes      | A query map, or a fn `resolved-deps -> query map`.            |
| `:depends-on` | no       | Dep keys that must resolve first.                             |
| `:retry`      | no       | Retry config, as for `::client/call`.                         |

Each dep's value is the `:result` of its response. The first failing dep
dispatches `:on-failure`. Cycles and unknown keys throw up-front.

## Translations

Translations are a map per language, passed to `core/init`:

```clojure
{:en {:greeting "Hello {name}!"
      :welcome  "Welcome {0} to {1}"
      :user     {:first-name "First Name"}}}
```

Translate with `edd.i18n/tr`:

```clojure
(tr :greeting)
(tr [:user :first-name])
(tr {:message :welcome :params ["Bob" "Berlin"]})
(tr {:message :greeting :params {:name "Bob"}})
```

## Upgrading

See [MIGRATION.md](MIGRATION.md) for breaking changes.

## License

[Apache 2.0](LICENSE), see [NOTICE](NOTICE). Contributions follow
[CONTRIBUTING](CONTRIBUTING.md).
