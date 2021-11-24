(ns web.primitives.inputs.subs
  (:require
   [re-frame.core :as rf]
   [goog.object :as g]))

(rf/reg-sub
 ::amount-scaling
 (fn []
   (or (g/get js/window "facilities-tab.amount-scaling") "mn")))