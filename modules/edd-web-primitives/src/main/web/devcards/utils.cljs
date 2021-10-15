(ns web.devcards.utils
  (:require
   [web.primitives.styles :as styles]
   ["@mui/material/CssBaseline" :default CssBaseline]

   ["@mui/styles" :refer [makeStyles withStyles]]
   ["@mui/material/styles" :refer [StyledEngineProvider alpha styled ThemeProvider createTheme adaptV4Theme]]))

(defn- raw-theme
  "Convert styles into javascript map"
  []
  (createTheme (clj->js
                (styles/theme))))

(defn apply-stiles [component]

  [:> ThemeProvider {:theme (createTheme (clj->js styles/theme))}
   [:> CssBaseline]
   component])

