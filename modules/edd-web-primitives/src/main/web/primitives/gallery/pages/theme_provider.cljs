(ns web.primitives.gallery.pages.theme-provider
  (:require
   [web.primitives.gallery.example :refer-macros [example]]
   [web.primitives.gallery.page :as page]
   [web.primitives.inputs.core :refer [RawButton]]
   [web.primitives.utils.core :refer [RawThemeProvider]]))

(def ^:private props-doc
  [{:name :theme :type "map" :default "{}" :doc "MUI theme options, passed to createTheme."}])

(defn register! []
  (page/register!
   {:id    :theme-provider
    :group "Utils"
    :title "Theme provider"
    :doc   "Applies an MUI theme to its children. Apps wrap their page in it with their own theme map."
    :props props-doc
    :variants
    [(example {:id :custom :title "Custom palette"}
              [RawThemeProvider {:theme {:palette {:primary {:main "#6d28d9"}}}}
               [RawButton {:el-id (page/el :theme-provider :custom) :variant "contained"}
                "Themed"]])]}))
