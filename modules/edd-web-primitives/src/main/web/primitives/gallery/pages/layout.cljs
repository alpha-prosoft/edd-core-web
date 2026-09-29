(ns web.primitives.gallery.pages.layout
  (:require
   [web.primitives.gallery.example :refer-macros [example]]
   [web.primitives.gallery.page :as page]
   [web.primitives.layout.core :refer [RawGrid RawBox RawStack]]))

(def ^:private props-doc
  [{:name :container       :type "boolean" :default "false" :doc "RawGrid: acts as a flex container."}
   {:name :size            :type "number | map" :default "—" :doc "RawGrid: columns (1-12) or breakpoint map."}
   {:name :xs              :type "number"  :default "—" :doc "RawGrid legacy prop, converted to :size."}
   {:name :spacing         :type "number"  :default "0" :doc "Gap between items."}
   {:name :direction       :type "string"  :default "\"row\"" :doc "column/column-reverse are moved to sx."}
   {:name :align-items     :type "string"  :default "—" :doc "Moved to sx automatically."}
   {:name :justify-content :type "string"  :default "—" :doc "Moved to sx automatically."}
   {:name :el-id           :type "keyword | string" :default "—" :doc "Lands as data-testid."}])

(defn register! []
  (page/register!
   {:id    :layout
    :group "Layout"
    :title "Layout (grid / box / stack)"
    :doc   "Layout helpers. RawGrid moves flex props that MUI v9 Grid no longer accepts (align-items, justify-content…) into sx, so old code keeps working."
    :props props-doc
    :variants
    [(example {:id :grid :title "Grid"}
              [RawGrid {:el-id (page/el :layout :grid) :container true :spacing 2}
               [RawGrid {:size 6} [RawBox {:style {:background "#eee" :padding "8px"}} "6"]]
               [RawGrid {:size 6} [RawBox {:style {:background "#ddd" :padding "8px"}} "6"]]])

     (example {:id :box :title "Box"}
              [RawBox {:el-id (page/el :layout :box) :style {:padding "16px" :background "#eef"}}
               "Box content"])

     (example {:id :stack :title "Stack"}
              [RawStack {:el-id (page/el :layout :stack) :direction "row" :spacing 2}
               [RawBox {:style {:background "#eee" :padding "8px"}} "a"]
               [RawBox {:style {:background "#ddd" :padding "8px"}} "b"]])]}))
