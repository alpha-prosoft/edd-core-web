(ns web.primitives.gallery.pages.icons
  (:require
   [web.primitives.gallery.example :refer-macros [defdemo example]]
   [web.primitives.gallery.page :as page]
   [web.primitives.icons.core :refer [EditIcon DeleteIcon AddIcon CloseIcon
                                      HomeIcon MenuIcon SearchIcon SettingsIcon]]))

(defdemo icon-cell [id icon]
  [:div {:data-testid (str "icon-" id)
         :style       {:display        "inline-flex"
                       :flex-direction "column"
                       :align-items    "center"
                       :width          "80px"
                       :margin         "8px"}}
   icon
   [:small id]])

(defn register! []
  (page/register!
   {:id    :icons
    :group "Data display"
    :title "Icons"
    :doc   "Material icons re-exported from web.primitives.icons.core. Each takes an MUI SvgIcon props map."
    :props [{:name :fontSize :type "small | medium | large | inherit" :default "\"medium\"" :doc "Icon size."}
            {:name :color    :type "string" :default "\"inherit\"" :doc "Theme color."}
            {:name :sx       :type "map"    :default "nil" :doc "Style overrides."}]
    :variants
    [(example {:id :grid :title "Icon grid"}
              [:div {:data-testid (page/testid :icons :grid)
                     :style       {:display "flex" :flex-wrap "wrap"}}
               [icon-cell "edit" [EditIcon {}]]
               [icon-cell "delete" [DeleteIcon {}]]
               [icon-cell "add" [AddIcon {}]]
               [icon-cell "close" [CloseIcon {}]]
               [icon-cell "home" [HomeIcon {}]]
               [icon-cell "menu" [MenuIcon {}]]
               [icon-cell "search" [SearchIcon {}]]
               [icon-cell "settings" [SettingsIcon {}]]])]}))
