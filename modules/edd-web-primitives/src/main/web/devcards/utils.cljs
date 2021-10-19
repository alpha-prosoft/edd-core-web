(ns web.devcards.utils
  (:require
   [web.primitives.styles :as styles]
   ["@mui/material/CssBaseline" :default CssBaseline]
   ["@mui/material/styles" :refer [StyledEngineProvider alpha styled ThemeProvider createTheme adaptV4Theme]]))

(defn apply-stiles [component]
  [:> ThemeProvider {:theme (createTheme (clj->js styles/theme))}
   [:> CssBaseline]
   component])

