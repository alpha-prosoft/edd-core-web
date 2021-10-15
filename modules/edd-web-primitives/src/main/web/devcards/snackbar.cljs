(ns web.devcards.snackbar
  (:require
   [cljsjs.react]
   [cljsjs.react.dom]
   [devcards.core :refer-macros (defcard-rg)]
   [reagent.core :as r]

   [web.devcards.utils :refer [apply-stiles]]
   [web.primitives.components :refer [RawSnackbar RawAlert RawGrid RawButton]]))

(defcard-rg :snackbar-alert
  "## Snackbar"
  (fn [data-atom _]
    (apply-stiles
     [RawGrid {:container true}
      [RawButton
       {:on-click #(swap! data-atom merge {:open true})}
       "Open snack bar"]
      [RawSnackbar {:open (:open @data-atom)
                    :on-close #(swap! data-atom merge {:open false})
                    :message "Some message"
                    :key "msg"}]]))
  (r/atom {:open false}))

(defcard-rg :snackbar-error-alert
  "## Snackbar with :error alert"
  (fn [data-atom _]
    (apply-stiles
     [RawGrid {:container true}
      [RawButton
       {:on-click #(swap! data-atom merge {:open true})}
       "Open snack bar"]
      [RawSnackbar {:open (:open @data-atom)
                    :on-close #(swap! data-atom merge {:open false})
                    :key "error"
                    :message "Error message"
                    :alert-props {:severity "error"}}]]))
  (r/atom {:open false}))

(defcard-rg :snackbar-info-alert
  "## Snackbar with :info alert"
  (fn [data-atom _]
    (apply-stiles
     [RawGrid {:container true}
      [RawButton
       {:on-click #(swap! data-atom merge {:open true})}
       "Open snack bar"]
      [RawSnackbar {:open (:open @data-atom)
                    :on-close #(swap! data-atom merge {:open false})
                    :key "info"
                    :message "Info message"
                    :alert-props {:severity "info"}}]]))
  (r/atom {:open false}))