(ns web.devcards.snackbar
  (:require
   [cljsjs.react]
   [cljsjs.react.dom]
   [devcards.core :refer-macros (defcard-rg)]
   [reagent.core :as r]

   [web.primitives.feedback.core :refer [RawSnackbar]]
   [web.primitives.lab.core :refer [RawAlert]]
   [web.primitives.layout.core :refer [RawGrid]]
   [web.primitives.inputs.core :refer [RawButton]]

   [web.devcards.utils :refer [apply-stiles]]))

(defcard-rg snackbar-alert
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

(defcard-rg snackbar-alert
  "## Snackbar with :error alert"
  (fn [data-atom _]
    (apply-stiles
     [RawGrid {:container true}
      [RawButton
       {:on-click #(swap! data-atom merge {:open true})}
       "Open snack bar"]
      [RawSnackbar {:open (:open @data-atom)
                    :on-close #(swap! data-atom merge {:open false})
                    :key "error"}
       [RawAlert {:severity :error} "Error message"]]]))
  (r/atom {:open false}))
