(ns web.devcards.alert
  (:require
   [reagent.core :as r]
   [cljsjs.react]
   [cljsjs.react.dom]
   [devcards.core :refer-macros (defcard-rg)]

   [web.devcards.utils :refer [apply-stiles]]
   [web.primitives.components :refer [RawGrid RawAlert RawIconButton]]
   [web.primitives.icons.core :refer [ClearIcon]]))

(defcard-rg :success-alert
  "## Success Alert"
  (apply-stiles [RawGrid {:container true}
                 [RawAlert
                  {:id       "alert-success"
                   :severity "success"}
                  "Success Alert"]]))

(defcard-rg :success-alert-with-title
  "## Success Alert with Title"
  (apply-stiles [RawGrid {:container true}
                 [RawAlert
                  {:id       "alert-success-with-title"
                   :severity "success"
                   :title    "Success"}
                  "Success Alert"]]))

(defcard-rg :error-alert
  "## Error Alert"
  (apply-stiles [RawGrid {:container true}
                 [RawAlert
                  {:id       "error-success"
                   :severity "error"}
                  "Error Alert"]]))

(defcard-rg :warning-alert
  "## Warning Alert"
  (apply-stiles [RawGrid {:container true}
                 [RawAlert
                  {:id       "warning-success"
                   :severity "warning"}
                  "Warning Alert"]]))

(defcard-rg :info-alert
  "## Info Alert with Grids"
  (apply-stiles [RawGrid {:container true}
                 [RawGrid {:item true :container true}
                  [RawAlert
                   {:id          "info-success"
                    :severity    "info"
                    :title       "Info Alert Title"
                    :title-props {:variant "h4"}
                    :action      (r/as-element [RawIconButton {} [ClearIcon]])}
                   [RawGrid {:container true :justify-content "space-between"}
                    [RawGrid {:item true} "Info Alert"]
                    [RawGrid {:container       true
                              :item            true
                              :xs              1
                              :alignContent    "center"
                              :justify-content "flex-end"}]]]]]))
