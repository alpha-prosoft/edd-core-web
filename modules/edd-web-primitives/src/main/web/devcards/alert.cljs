(ns web.devcards.alert
  (:require
   [cljsjs.react]
   [cljsjs.react.dom]
   [devcards.core :refer-macros (defcard-rg)]
   ["@material-ui/core" :refer [Grid]]

   [web.primitives.icons.core :refer [ClearIcon]]
   [web.primitives.lab.core :refer [RawAlert]]
   [web.devcards.utils :refer [apply-stiles]]))

(defcard-rg success-alert
  "## Success Alert"
  (apply-stiles [:> Grid {:container true}
                 [RawAlert
                  {:id "alert-success"
                   :severity "success"}
                  "Success Alert"]]))

(defcard-rg success-alert-with-title
  "## Success Alert with Title"
  (apply-stiles [:> Grid {:container true}
                 [RawAlert
                  {:id "alert-success-with-title"
                   :severity "success"
                   :title "Success"}
                  "Success Alert"]]))

(defcard-rg error-alert
  "## Error Alert"
  (apply-stiles [:> Grid {:container true}
                 [RawAlert
                  {:id "error-success"
                   :severity "error"}
                  "Error Alert"]]))

(defcard-rg warning-alert
  "## Warning Alert"
  (apply-stiles [:> Grid {:container true} [RawAlert
                                            {:id "warning-success"
                                             :severity "warning"}
                                            "Warning Alert"]]))

(defcard-rg info-alert
  "## Info Alert with Grids"
  (apply-stiles [:> Grid {:container true}
                 [:> Grid {:item true :container true}
                  [RawAlert
                   {:id "info-success"
                    :severity "info"}
                   [:> Grid {:container true :justify-content "space-between"}
                    [:> Grid {:item true} "Info Alert"]
                    [:> Grid {:container true
                              :item true
                              :xs 1
                              :alignContent "center"
                              :justify-content "flex-end"}
                     [ClearIcon]]]]]]))
