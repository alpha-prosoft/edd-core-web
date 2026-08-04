(ns web.card
  (:require

   [devcards.core :refer-macros (defcard-rg)]
   [web.primitives.utils :refer [apply-stiles]]
   [web.primitives.components :refer [RawGrid RawCard RawButton]]
   [reagent.core :as r]))

(defcard-rg :card
  "## Card"
  (apply-stiles [RawCard
                 {:header  {:title     "Header"
                            :subheader "Subheader"
                            :action    (r/as-element [RawButton {:on-click #(print "action")} "actions"])}
                  :actions [RawGrid {:container true} [RawButton {:on-click #(print "action")} "actions"]]}

                 [RawGrid {:container true :sx {:background-color "#ececec"}}
                  [RawGrid {}
                   "Content"]]]))

(defcard-rg :card-with-on-click-event
  "## Clickable Card"
  (apply-stiles [RawCard
                 {:header  {:title     "Header"
                            :subheader "Subheader"}
                  :on-click #(print "on-card-click")
                  :actions [RawGrid {:container true}
                            [RawButton {:on-click #(print "action")} "actions"]]}

                 [RawGrid {:container true :sx {:background-color "#ececec"}}
                  [RawGrid {}
                   "Content"]]]))
