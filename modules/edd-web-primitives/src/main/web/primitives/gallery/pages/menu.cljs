(ns web.primitives.gallery.pages.menu
  (:require
   [reagent.core :as r]
   [web.primitives.gallery.example :refer-macros [defdemo example]]
   [web.primitives.gallery.page :as page]
   [web.primitives.data-display.core :refer [RawMenu RawMenuItem]]
   [web.primitives.inputs.core :refer [RawButton]]))

(defdemo menu []
  (let [state (r/atom {:open false :anchor nil :picked nil})]
    (fn []
      [:div
       [RawButton {:el-id    (page/el :menu :open)
                   :variant  "contained"
                   :on-click (fn [^js e] (swap! state assoc :open true :anchor (.-currentTarget e)))}
        "Open menu"]
       [RawMenu {:el-id    (page/el :menu :menu)
                 :open     (:open @state)
                 :anchorEl (:anchor @state)
                 :onClose  #(swap! state assoc :open false)}
        [RawMenuItem {:el-id    (page/el :menu :item-a)
                      :on-click #(swap! state assoc :open false :picked "A")}
         "A"]
        [RawMenuItem {:el-id    (page/el :menu :item-b)
                      :on-click #(swap! state assoc :open false :picked "B")}
         "B"]]
       [:div {:data-testid (page/testid :menu :menu :readout)}
        (str "picked:" (:picked @state))]])))

(def ^:private props-doc
  [{:name :open     :type "boolean" :default "false" :doc "Visibility (coerced to boolean)."}
   {:name :anchorEl :type "DOM node" :default "nil"  :doc "Element the menu attaches to."}
   {:name :onClose  :type "fn"      :default "—"     :doc "Close handler."}
   {:name :on-click :type "fn"      :default "—"     :doc "RawMenuItem: click handler."}
   {:name :el-id    :type "keyword | string" :default "—" :doc "Lands as data-testid."}])

(defn register! []
  (page/register!
   {:id    :menu
    :group "Surfaces"
    :title "Menu"
    :doc   "A popup list of actions anchored to an element."
    :props props-doc
    :variants
    [(example {:id :basic :title "Basic"}
              [menu])]}))
