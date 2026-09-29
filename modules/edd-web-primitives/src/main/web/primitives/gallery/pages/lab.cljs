(ns web.primitives.gallery.pages.lab
  (:require
   [reagent.core :as r]
   [web.primitives.gallery.example :refer-macros [defdemo example]]
   [web.primitives.gallery.page :as page]
   [web.primitives.lab.core :refer [RawAlert RawSkeleton RawToggleButtonGroup RawToggleButton RawTimelineDot RawSimpleTreeView RawTreeItem]
    :rename {RawSimpleTreeView RawTreeView}]))

(defdemo toggle-group []
  (let [state (r/atom {:value "left"})]
    (fn []
      [:div
       [RawToggleButtonGroup {:el-id     (page/el :lab :toggle)
                              :value     (:value @state)
                              :exclusive true
                              :onChange  (fn [^js _e v] (when v (swap! state assoc :value v)))}
        [RawToggleButton {:value "left"} "Left"]
        [RawToggleButton {:value "right"} "Right"]]
       [:div {:data-testid (page/testid :lab :toggle :readout)}
        (str "value:" (:value @state))]])))

(def ^:private props-doc
  [{:name :severity  :type "success | info | warning | error" :default "—" :doc "Alert: type and color."}
   {:name :title     :type "node"    :default "nil"   :doc "Alert: optional heading."}
   {:name :variant   :type "string"  :default "—"     :doc "Skeleton: text | rectangular | circular. TimelineDot: \"default\" maps to \"filled\"."}
   {:name :exclusive :type "boolean" :default "false" :doc "ToggleButtonGroup: single selection."}
   {:name :itemId    :type "string"  :default "—"     :doc "TreeItem: unique id."}
   {:name :label     :type "node"    :default "—"     :doc "TreeItem: text."}
   {:name :el-id     :type "keyword | string" :default "—" :doc "Lands as data-testid."}])

(defn register! []
  (page/register!
   {:id    :lab
    :group "Lab"
    :title "Lab (alert / skeleton / toggle / timeline / tree)"
    :doc   "Assorted components from MUI lab and x-tree-view."
    :props props-doc
    :variants
    [(example {:id :alert :title "Alert"}
              [RawAlert {:el-id (page/el :lab :alert) :severity "success"} "All good"])

     (example {:id :skeleton :title "Skeleton"}
              [RawSkeleton {:el-id   (page/el :lab :skeleton)
                            :variant "rectangular"
                            :width   200
                            :height  40}])

     (example {:id :toggle :title "Toggle button group"}
              [toggle-group])

     (example {:id :timeline :title "Timeline dot"}
              [RawTimelineDot {:el-id (page/el :lab :timeline) :variant "default"}])

     (example {:id :tree :title "Tree view"}
              [RawTreeView {:el-id (page/el :lab :tree)}
               [RawTreeItem {:itemId "1" :label "Root"}
                [RawTreeItem {:itemId "2" :label "Child"}]]])]}))
