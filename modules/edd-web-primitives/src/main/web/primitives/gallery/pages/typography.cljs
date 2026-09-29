(ns web.primitives.gallery.pages.typography
  (:require
   [web.primitives.gallery.example :refer-macros [example]]
   [web.primitives.gallery.page :as page]
   [web.primitives.data-display.core :refer [RawTypography]]))

(def ^:private props-doc
  [{:name :variant     :type "h1..h6 | subtitle1 | subtitle2 | body1 | body2 | caption | overline" :default "\"body1\"" :doc "Typographic scale."}
   {:name :align       :type "string" :default "\"inherit\"" :doc "Text alignment."}
   {:name :color       :type "string" :default "—" :doc "Moved to sx automatically."}
   {:name :text-align  :type "string" :default "—" :doc "Legacy prop, moved to sx."}
   {:name :white-space :type "string" :default "—" :doc "Legacy prop, moved to sx."}
   {:name :el-id       :type "keyword | string" :default "—" :doc "Lands as data-testid."}])

(defn register! []
  (page/register!
   {:id    :typography
    :group "Data display"
    :title "Typography"
    :doc   "Text styles from the theme. Legacy layout props (color, text-align, display…) are moved into `sx` for you."
    :props props-doc
    :variants
    [(example {:id :h1 :title "h1"}
              [RawTypography {:el-id (page/el :typography :h1) :variant "h1"} "h1 sample"])
     (example {:id :h2 :title "h2"}
              [RawTypography {:el-id (page/el :typography :h2) :variant "h2"} "h2 sample"])
     (example {:id :h3 :title "h3"}
              [RawTypography {:el-id (page/el :typography :h3) :variant "h3"} "h3 sample"])
     (example {:id :h4 :title "h4"}
              [RawTypography {:el-id (page/el :typography :h4) :variant "h4"} "h4 sample"])
     (example {:id :h5 :title "h5"}
              [RawTypography {:el-id (page/el :typography :h5) :variant "h5"} "h5 sample"])
     (example {:id :h6 :title "h6"}
              [RawTypography {:el-id (page/el :typography :h6) :variant "h6"} "h6 sample"])
     (example {:id :subtitle1 :title "subtitle1"}
              [RawTypography {:el-id (page/el :typography :subtitle1) :variant "subtitle1"} "subtitle1 sample"])
     (example {:id :subtitle2 :title "subtitle2"}
              [RawTypography {:el-id (page/el :typography :subtitle2) :variant "subtitle2"} "subtitle2 sample"])
     (example {:id :body1 :title "body1"}
              [RawTypography {:el-id (page/el :typography :body1) :variant "body1"} "body1 sample"])
     (example {:id :body2 :title "body2"}
              [RawTypography {:el-id (page/el :typography :body2) :variant "body2"} "body2 sample"])
     (example {:id :caption :title "caption"}
              [RawTypography {:el-id (page/el :typography :caption) :variant "caption"} "caption sample"])
     (example {:id :overline :title "overline"}
              [RawTypography {:el-id (page/el :typography :overline) :variant "overline"} "overline sample"])]}))
