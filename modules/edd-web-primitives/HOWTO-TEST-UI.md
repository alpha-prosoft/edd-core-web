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


## Devcards testing
### How to write components
Sadly devcards dont work very well with re-frame because of its global app-db.
It is doable but require some hacking. We can go simpler way - write components
in a little bit different way.

Currently components are using re-frame subscribers and dispatch function right
in the function body:

```clojurescript
(defn main-panel
  [classes]
  (let [logged-in @(rf/subscribe [::umsubs/user-is-logged-in])]
    ...
     [:> Select
            {:value     @(rf/subscribe [::subs/selected-consolidation-point])
             :on-change #(rf/dispatch [::events/consolidation-point-selected (-> % .-target .-value)])}])
    ...
  )

```

We can do something like this:

1) The component which know where data comes from (re-frame aware, used in application)
```clojurescript
(defn main-panel
  [classes]
  (main-panel-component classes
                        {:user-is-logged-in
                         @(rf/subscribe [::umsubs/user-is-logged-in])
                         :subscribe-cp
                         @(rf/subscribe [::subs/selected-consolidation-point])
                         :dispatch-cp-selected #(rf/dispatch [::events/consolidation-point-selected (-> % .-target .-value)])}))
```

2) Pure component which renders data and perfectly can be used in devcards:

```clojurescript
(defn main-panel-component
  [classes {:keys [user-is-logged-in subscribe-cp dispatch-cp-selected]}]
  (let [logged-in user-is-logged-in]
    ...
     [:> Select {:value subscribe-cp :on-change dispatch-cp-selected}])
    ...
  )
```

*Caution: currently is an idea example code could be broken!*



### How to write devcards

#### WHY:
Devcards are sandbox environment for UI components where we can check all
edge cases of our components. This is done to make sure you component can
display provided data correctly. Devcards can also be used as a UI component
catalog and for QA.

#### HOW:

1) You need to create a namespace for you component devcards:
```clojurescript
(ns primitives.devcards-example
  (:require
   [cljsjs.react]
   [cljsjs.react.dom]
   [devcards.core :refer-macros (defcard defcard-rg)]
   ["@material-ui/core" :refer [Button Card CardContent Typography]]
   ["@material-ui/icons/Menu" :default Menu]
   ["@material-ui/core/styles/withStyles" :default withStyles]
   [reagent.core :as r]))

(defcard-rg simple-button-component
  "## Material UI Button"
  [:> Button {:variant
              :contained} "button"])
```

2) You need to **require** your namespace in **start_ui.cljs***

```clojurescript
(ns primitives.start-ui
  (:require
   [cljsjs.react]
   [cljsjs.react.dom]
   [devcards.core :as dc]
   [primitives.devcards-example]))
```

For more info: [devcards](https://github.com/bhauman/devcards)



### How to write devcards test

#### WHY:
Someone made a change to css, we need to make sure all our components still
rendered in desired way, nothing is overlapping.
We should do visual confirmation by going through all devcards making sure nothing is broken .

#### HOW
Going after each change through all devcards is tedious task. That's why we have automated it.
As a test dependency you can include artifact:
**{org/glms-ui-testkit {:mvn/version "1.0.b9"}}**
After calling **c/run-devcard-test** following is happening:

1) Project npm dependencies are installed
2) Project artifact is compiled
3) Project artifact is started
4) After project is running, all devcards groups screenshots are made
5) Comparing all screenshots with catalog values

*Caution: currently whole group of devcards is checked and not single devcards!*

```clojure
(ns primitives.devcards-test
  (:require [clojure.test :refer [deftest]]
            [ui-testkit.core :as c]))

(def default-opts
  {:screenshot-dir "target/devcards"
   :reference-dir "src/test/resources/devcards"
   :build-id :devcards
   :aliases [:dev]
   :metric-threshold 0.001
   :verbose true
   :assert? true})

(deftest when-devcards-ok
  (c/run-devcard-tests default-opts))
```
*Caution: this is a clojure file and not a clojurescript!*

For manual start you can run from console:
```bash
clojure -A:test:runner
```

Options:

| Key               | Meaning                                                                  | Default                              |
| -------------     | :-------------:                                                          | -----:                               |
| :reference-dir    | catalog image directory, files names must correspond to devcard group-id | "#{proj_dir}test/resources/devcards" |
| :screenshot-dir   | working directory, where screenshots and image diffs will be saved       | "${proj_dir}target/devcards"         |
| :aliases          | deps aliases, f.e dev,test                                               | []                                   |
| :assert?          | should results of image comparing be asserted                            | true                                 |
| :metric-threshold | tolerated image difference                                               | 0.001                                |
| :build-id         | devcards artifact                                                        | :devcards                            |
