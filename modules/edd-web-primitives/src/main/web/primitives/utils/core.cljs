(ns web.primitives.utils.core
  (:require
   [web.primitives.utils :as utils]
   ["@mui/material/Zoom" :default Zoom]
   ["@mui/material/Slide" :default Slide]
   ["@mui/material/Collapse" :default Collapse]
   ["@mui/material/GlobalStyles" :default GlobalStyles]
   ["@mui/material/styles" :refer [ThemeProvider createTheme]]))

(defn RawSlide [{:keys [in direction timeout el-id]
                 :or   {in false direction "left" timeout 500}}
                content]
  [:> Slide (utils/with-el-id-as-testid
              {:in            in
               :direction     direction
               :mountOnEnter  true
               :unmountOnExit true
               :timeout       timeout
               :el-id         el-id})
   [:div content]])

(defn RawZoom [{:keys [in] :or {in false} :as props} content]
  [:> Zoom (merge
            {:in in}
            (utils/with-el-id-as-testid props))
   [:div content]])

(defn RawCollapse [{:keys [in] :or {in false} :as props} & children]
  (into
   [:> Collapse (merge (utils/with-el-id-as-testid props) {:in (boolean in)})]
   children))

(defn RawGlobalStyles [props]
  [:> GlobalStyles props])

(defn RawThemeProvider [{:keys [theme]} & children]
  (into
   [:> ThemeProvider {:theme (createTheme (clj->js theme))}]
   children))
