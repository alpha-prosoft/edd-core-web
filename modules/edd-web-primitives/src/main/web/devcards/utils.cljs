(ns web.devcards.utils
  (:require
   [web.primitives.styles :as styles]
   ["@mui/material/CssBaseline" :default CssBaseline]
   ["@mui/material/styles" :refer [ThemeProvider createTheme]]))

(defn apply-stiles [component]
  [:> ThemeProvider {:theme (createTheme (clj->js styles/theme))}
   [:> CssBaseline]
   component])

