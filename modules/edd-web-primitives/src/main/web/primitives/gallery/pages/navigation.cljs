(ns web.primitives.gallery.pages.navigation
  (:require
   [reagent.core :as r]
   [web.primitives.gallery.example :refer-macros [defdemo example]]
   [web.primitives.gallery.page :as page]
   [web.primitives.inputs.core :refer [RawButton]]
   [web.primitives.navigation.core :refer [RawTabs RawLink RawDrawer]]))

(defdemo tabs []
  (let [state (r/atom {:value 0})]
    (fn []
      [:div
       [RawTabs {:el-id     (page/el :navigation :tabs)
                 :value     (:value @state)
                 :on-change (fn [^js _e v] (swap! state assoc :value v))
                 :tabs      [{:label "One"} {:label "Two"} {:label "Three"}]}]
       [:div {:data-testid (page/testid :navigation :tabs :readout)}
        (str "value:" (:value @state))]])))

(defdemo drawer []
  (let [state (r/atom {:open false})]
    (fn []
      [:div
       [RawButton {:el-id    (page/el :navigation :drawer-open)
                   :variant  "contained"
                   :on-click #(swap! state assoc :open true)}
        "Open drawer"]
       [RawDrawer {:el-id   (page/el :navigation :drawer)
                   :open    (:open @state)
                   :onClose #(swap! state assoc :open false)}
        [:div {:style {:width "200px" :padding "16px"}} "Drawer content"]]])))

(def ^:private props-doc
  [{:name :tabs                :type "vector of Tab props" :default "—" :doc "RawTabs: one map per tab, e.g. {:label \"One\"}."}
   {:name :value               :type "any"     :default "—"     :doc "RawTabs: selected tab index/value."}
   {:name :on-change           :type "fn"      :default "—"     :doc "RawTabs: (event, value) handler."}
   {:name :tab-indicator-props :type "map"     :default "nil"   :doc "RawTabs: moved into slotProps.indicator."}
   {:name :href                :type "string"  :default "—"     :doc "RawLink: target URL."}
   {:name :open                :type "boolean" :default "false" :doc "Drawer: visibility."}
   {:name :anchor              :type "string"  :default "\"left\"" :doc "Drawer: side."}
   {:name :onClose             :type "fn"      :default "—"     :doc "Drawer: close handler."}
   {:name :el-id               :type "keyword | string" :default "—" :doc "Lands as data-testid."}])

(defn register! []
  (page/register!
   {:id    :navigation
    :group "Navigation"
    :title "Navigation (tabs / link / drawer)"
    :doc   "Move between views. `RawSwipeableDrawer` works like RawDrawer and adds swipe support."
    :props props-doc
    :variants
    [(example {:id :tabs :title "Tabs"}
              [tabs])

     (example {:id :link :title "Link"}
              [RawLink {:el-id (page/el :navigation :link) :href "#/navigation"} "A link"])

     (example {:id :drawer :title "Drawer"}
              [drawer])]}))
