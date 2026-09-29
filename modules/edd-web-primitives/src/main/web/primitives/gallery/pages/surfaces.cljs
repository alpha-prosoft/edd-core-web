(ns web.primitives.gallery.pages.surfaces
  (:require
   [reagent.core :as r]
   [web.primitives.gallery.example :refer-macros [example]]
   [web.primitives.gallery.page :as page]
   [web.primitives.data-display.core :refer [RawTypography]]
   [web.primitives.surfaces.core :refer [RawCard EddAccordion RawAppBar RawToolbar]]))

(def ^:private props-doc
  [{:name :header           :type "map | element" :default "nil" :doc "Card: CardHeader props. Accordion: summary content."}
   {:name :media            :type "map"     :default "nil"   :doc "Card: CardMedia props."}
   {:name :actions          :type "node"    :default "nil"   :doc "Card: footer actions."}
   {:name :on-click         :type "fn"      :default "nil"   :doc "Card: makes the card clickable."}
   {:name :elevation        :type "number"  :default "3 / 0" :doc "Shadow depth (card 3, accordion 0)."}
   {:name :expanded?        :type "boolean" :default "false" :doc "RawAccordion: controlled expanded state (EddAccordion manages it)."}
   {:name :control-position :type ":left | :right" :default ":left" :doc "Accordion: toggle button side."}
   {:name :header-expanded  :type "element" :default "nil"   :doc "Accordion: header shown while expanded."}
   {:name :position         :type "string"  :default "\"fixed\"" :doc "AppBar: CSS position."}
   {:name :el-id            :type "keyword | string" :default "—" :doc "Lands as data-testid."}])

(defn register! []
  (page/register!
   {:id    :surfaces
    :group "Surfaces"
    :title "Surfaces (card / accordion / app-bar)"
    :doc   "Containers that group content. `EddAccordion` keeps its expanded state in re-frame."
    :props props-doc
    :variants
    [(example {:id :card :title "Card"}
              [RawCard {:el-id (page/el :surfaces :card) :header {:title "Card title"}}
               [RawTypography {} "Card content"]])

     (example {:id :accordion :title "Accordion"}
              [EddAccordion {:el-id  (page/el :surfaces :accordion)
                             :header (r/as-element [RawTypography {} "Section"])}
               [RawTypography {} "Accordion details content"]])

     (example {:id :app-bar :title "App bar"}
              [RawAppBar {:el-id (page/el :surfaces :app-bar) :position "static"}
               [RawToolbar {} [RawTypography {} "Title"]]])]}))
