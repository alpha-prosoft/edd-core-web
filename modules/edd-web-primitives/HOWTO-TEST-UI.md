## Unit testing

We need to test the logic of our application.

### Subscribers testing

#### WHY:
If subscriber has some business logic which we want to test
(f.e some data transformation is done after loading from db)
or we want to validate the data with a spec. Additionally we can
assert the app-db.

#### HOW:
We can setup desired db state by dispatching events.
**(if the handlers have effects other than just updating app-db we might need to stub it out)***

In the following example we are testing the subscriber for ::subs/consolidation-points
There is no logic in this subscriber - values are just read from app-db.

1) we stub external service call (**:call**) which will return some mocked data
2) we subscribe to **::subs/consolidation-points**
3) we dispatch an event (or multiple events)
4) we can assert the data from subscriber

Example:
```clojurescript
(deftest when-consolidation-point-sub
  (run-test-sync
   (re-frame/reg-fx
    :call
    (fn [{:keys [on-success]}]
      (re-frame/dispatch (conj on-success {:result "<consolidation-points>"}))))

   (let [t (re-frame/subscribe [::subs/consolidation-points])]
     (re-frame/dispatch [:initialize-cp-db])

     ;; we can write tests against db
     (pprint @re-frame.db/app-db)

     (testing "consolidation points are loaded"
       (is (= "<consolidation-points>" @t))))))
```

### Event handler

#### WHY:
If event handler has some business logic which we want to test
(f.e some data transformation is done before saving data to app-db)
or we want to validate the data with a spec.

#### HOW:
Instead of doing event handler function as anonymous, we can define as a normal function.
This allows us test the outcome of this function.

Before:
```clojurescript
(re-frame/reg-event-fx
  :initialize-cp-db
 (fn [{:keys [db]} _]
   {:db   (merge db/default-db db)
    :call {:query      {:query-id :list-consolidation-points}
           :service    :glms-consolidation-point-svc
           :on-success [::consolidation-points-loaded]}}))
```
After:
```clojurescript
(defn initialize-cp-db
  "Handles :initialize-cp-db event"
  [{:keys [db]} _]
  {:db   (merge db/default-db db)
   :call {:query      {:query-id :list-consolidation-points}
          :service    :glms-consolidation-point-svc
          :on-success [::consolidation-points-loaded]}})

(re-frame/reg-event-fx
 :initialize-cp-db
 initialize-cp-db)
```

In this test you can see how the event handler function is called to obtain
the app-db and assert is written against app-db. We can go further and chain the
event handlers calls to obtain desired app-db state. 

*Caution: chaining event handlers with effects will fast become messy*

Example:
```clojurescript
(deftest when-initialize-cp-db-then-db-is-correct
  (run-test-sync

         event [:initialize-cp-db]
         result-db (events/initialize-cp-db db event)]
     (testing "database check"
       (is (= {:db {:primitives.cp.db/name "cp",
                    :primitives.cp.db/clicks 0,
                    :primitives.cp.db/current-date "2020-03-25",
                    :primitives.cp.db/time-range 50,
                    :primitives.cp.db/selected-consolidation-point "None",
                    :primitives.cp.db/consolidation-points [],
                    :primitives.cp.db/timeline [],
                    :primitives.cp.db/controls {:limit true,
                                             :facility true,
                                             :actual-exposure false,
                                             :MPE false,
                                             :availability false,
                                             :excess false}},
               :call {:query {:query-id :list-consolidation-points},
                      :service :glms-consolidation-point-svc,
                      :on-success [:primitives.cp.events/consolidation-points-loaded]}}
              result-db))))))
```


## UI testing with the UI SDK

UI tests drive a real browser with Playwright. They never use CSS
selectors, MUI class names or XPath. Instead:

1. Every primitive accepts an `:el-id`.
2. The app defines its element ids once in `elements.cljc`, shared by the
   views (ClojureScript) and the tests (JVM).
3. Tests call **SDK functions** with those ids. The SDK knows how each
   primitive is built in the DOM (hidden inputs, portals, MUI v9 sectioned
   date fields, SunEditor...).

### Artifacts

| Artifact                     | Contains                                 | Use from        |
|------------------------------|------------------------------------------|-----------------|
| `edd-web-primitives`         | the primitives (cljs) + `el-id` helpers  | the app (cljs)  |
| `edd-web-primitives-ui-sdk`  | `web.primitives.sdk.*` + `el-id` helpers | the tests (clj) |

The SDK only depends on Clojure and `com.microsoft.playwright/playwright`.
Any runner works (clojure.test, kaocha, clojure-playwright).

### 1. Give elements an id

```clojure
;; src/cljc/my/app/order/elements.cljc
(ns my.app.order.elements
  (:require [web.primitives.testing.el-id :refer [el-id]]))

(defn customer   [] (el-id ::customer))
(defn due-date   [] (el-id ::due-date))
(defn urgent     [] (el-id ::urgent))
(defn save       [] (el-id ::save))
(defn saved-toast [] (el-id ::saved-toast))
(defn line-row   [n] (el-id ::line-row n))   ; => ".../line-row--3"
```

```clojure
;; view
[RawAutocomplete {:el-id (els/customer) :options customers ...}]
[EddDatePicker   {:el-id (els/due-date) ...}]
[RawCheckbox     {:el-id (els/urgent) :label "Urgent" ...}]
[RawButton       {:el-id (els/save) :variant "contained" ...} "Save"]
[RawSnackbar     {:el-id (els/saved-toast) :open saved? :message "Order saved" ...}]
```

### 2. Write the test with SDK functions

```clojure
(ns my.app.order.order-test
  (:require [clojure.test :refer [deftest is]]
            [my.app.order.elements :as els]
            [web.primitives.sdk.core :as sdk]
            [web.primitives.sdk.autocomplete :as autocomplete]
            [web.primitives.sdk.date-picker :as date-picker]
            [web.primitives.sdk.checkbox :as checkbox]
            [web.primitives.sdk.button :as button]
            [web.primitives.sdk.snackbar :as snackbar]))

(deftest create-urgent-order
  (sdk/with-page page                       ; any Playwright Page
    (sdk/goto! "http://localhost:3000/#/orders/new")

    (autocomplete/choose! (els/customer) "ACME")
    (date-picker/set-date! (els/due-date) "15.05.2027")
    (checkbox/check!       (els/urgent))
    (button/click!         (els/save))

    (is (= "Order saved" (snackbar/message (els/saved-toast))))))
```

With clojure-playwright (traces for every test):

```clojure
(deftest create-urgent-order
  (with-ui [page]
    (sdk/with-page page
      ...)))
```

### Namespaces

| Namespace                        | For                                       | Main functions |
|----------------------------------|-------------------------------------------|----------------|
| `sdk.core`                       | binding the page, generic helpers         | `with-page` `goto!` `el` `click!` `text` `visible?` `becomes-visible?` `becomes-hidden?` `press!` `eventually` |
| `sdk.button`                     | RawButton, RawFab, RawIconButton          | `click!` `enabled?` `label` |
| `sdk.checkbox`                   | RawCheckbox                               | `checked?` `check!` `uncheck!` `set-checked!` `toggle!` |
| `sdk.switch`                     | RawSwitch (incl. two-value)               | `on?` `turn-on!` `turn-off!` `toggle!` |
| `sdk.text-field`                 | RawTextField, RawNumberField, RawPercentField | `fill!` `type!` `clear!` `value` `blur!` `label` `helper-text` `read-only?` |
| `sdk.select`                     | RawSelect, RawFormSelect                  | `choose!` `selected` `options` |
| `sdk.autocomplete`               | RawAutocomplete                           | `search!` `choose!` `visible-options` `value` `chips` `clear!` |
| `sdk.radio`                      | RawRadioGroup                             | `choose!` `selected` `values` |
| `sdk.slider`                     | RawSlider                                 | `set-value!` `value` `min-value` `max-value` |
| `sdk.date-picker`                | EddDatePicker                             | `set-date!` `type-date!` `value` `blank?` `clear!` `blur!` `error?` `helper-text` `open-calendar!` |
| `sdk.text-editor`                | EddTextEditor                             | `editing?` `start-editing!` `type!` `text` `save!` `discard!` |
| `sdk.dialog`                     | RawDialog                                 | `wait-open!` `wait-closed!` `open?` `title` `body-text` `close-with-escape!` |
| `sdk.snackbar`                   | RawSnackbar                               | `message` `open?` `wait-closed!` |
| `sdk.menu`                       | RawMenu, RawMenuItem                      | `items` `choose!` `wait-open!` `wait-closed!` |
| `sdk.tabs`                       | RawTabs                                   | `choose!` (label or index) `selected` `selected-index` `labels` |
| `sdk.drawer`                     | RawDrawer, RawSwipeableDrawer             | `content-text` `open?` `close-with-escape!` |
| `sdk.accordion`                  | RawAccordion, EddAccordion                | `expand!` `collapse!` `expanded?` `details-text` |
| `sdk.toggle-button`              | RawToggleButtonGroup                      | `choose!` `pressed?` `selected` |
| `sdk.tooltip`                    | RawTooltip                                | `text` `show!` `hide!` |
| `sdk.display`                    | typography, badge, chip, avatar, alert, list, table, tree, card, progress | `text` `tag` `badge-content` `alert-severity` `list-items` `table-headers` `table-rows` `tree-items` `expand-tree-item!` `card-title` |
| `sdk.console`                    | smoke tests                               | `watch-errors!` `page-errors` `console-errors` |

### Rules

- **Wait on the state, not the clock.** MUI animates dialogs, drawers,
  backdrops and transitions. After an action that shows or hides
  something, use `becomes-visible?` / `becomes-hidden?` or a `wait-*`
  function, never `visible?` straight after a click.
- **Labels are what you passed**, not what CSS shows (`button/label`
  returns `"Save"`, not the upper-cased `"SAVE"`).
- **Missing an action?** Add it to the SDK namespace for that primitive,
  not to your test. Every consumer of the primitive then gets it.

### Running the gallery tests in this repo

```
make gallery   # serve the component browser on http://localhost:3002
make it        # run the Playwright suite against it (traces in target/pw-traces)
make jars      # build both artifacts into target/release-libs
```
