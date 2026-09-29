(ns web.primitives.gallery.pages.transitions
  (:require
   [reagent.core :as r]
   [web.primitives.gallery.example :refer-macros [defdemo example]]
   [web.primitives.gallery.page :as page]
   [web.primitives.inputs.core :refer [RawCheckbox]]
   [web.primitives.utils.core :refer [RawZoom RawSlide]]))

(defdemo zoom []
  (let [state (r/atom {:in true})]
    (fn []
      [:div
       [RawCheckbox {:el-id     (page/el :transitions :zoom-toggle)
                     :checked   (:in @state)
                     :label     "Show"
                     :on-change #(swap! state update :in not)}]
       [RawZoom {:el-id (page/el :transitions :zoom) :in (:in @state)}
        [:div {:style {:padding "16px" :background "#eef"}} "Zoomed box"]]])))

(defdemo slide []
  (let [state (r/atom {:in true})]
    (fn []
      [:div
       [RawCheckbox {:el-id     (page/el :transitions :slide-toggle)
                     :checked   (:in @state)
                     :label     "Show"
                     :on-change #(swap! state update :in not)}]
       [RawSlide {:el-id (page/el :transitions :slide) :in (:in @state) :direction "right"}
        [:div {:style {:padding "16px" :background "#efe"}} "Sliding box"]]])))

(def ^:private props-doc
  [{:name :in        :type "boolean" :default "false"  :doc "Show (true) or hide (false) the child."}
   {:name :direction :type "left | right | up | down" :default "\"left\"" :doc "RawSlide: slide direction."}
   {:name :timeout   :type "ms"      :default "500"    :doc "RawSlide: animation duration."}
   {:name :el-id     :type "keyword | string" :default "—" :doc "Lands as data-testid."}])

(defn register! []
  (page/register!
   {:id    :transitions
    :group "Utils"
    :title "Transitions (zoom / slide)"
    :doc   "Animate content in and out. The child is wrapped in a div so any hiccup works."
    :props props-doc
    :variants
    [(example {:id :zoom :title "Zoom"}
              [zoom])

     (example {:id :slide :title "Slide"}
              [slide])]}))
