(ns web.primitives.utils
  (:require
   [web.primitives.styles :as styles]
   ["@mui/material/CssBaseline" :default CssBaseline]
   ["@mui/material/styles" :refer [ThemeProvider createTheme]]))

(defn apply-stiles [component]
  [:> ThemeProvider {:theme (createTheme (clj->js styles/theme))}
   [:> CssBaseline]
   component])

(defn handle-paper-props-migration [{:keys [PopperProps popper-props] :as props}]
  (cond->
   (dissoc props
           :PopperProps
           :popper-props)
    (or (some? PopperProps)
        (some? popper-props))
    (assoc-in [:slotProps :paper] (merge PopperProps popper-props))))

(defn handle-tab-indicator-props-migration [{:keys [TabIndicatorProps tab-indicator-props] :as props}]
  (cond->
   (dissoc props
           :TabIndicatorProps
           :tab-indicator-props)
    (or (some? TabIndicatorProps)
        (some? tab-indicator-props))
    (assoc-in [:slotProps :tabIndicator] (merge TabIndicatorProps tab-indicator-props))))

(defn handle-root-styles-migration [{:keys [text-align flex-direction align-items overflow
                                            text-overflow white-space] :as props}]
  (cond->
   (dissoc props
           :text-align
           :flex-direction
           :align-items
           :overflow
           :text-overflow
           :white-space)
    (some? text-align) (assoc-in [:sx :text-align] text-align)
    (some? flex-direction) (assoc-in [:sx :flex-direction] flex-direction)
    (some? align-items) (assoc-in [:sx :align-items] align-items)
    (some? overflow) (assoc-in [:sx :overflow] overflow)
    (some? text-overflow) (assoc-in [:sx :text-overflow] text-overflow)
    (some? white-space) (assoc-in [:sx :white-space] white-space)))