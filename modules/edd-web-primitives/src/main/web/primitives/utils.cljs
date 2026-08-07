(ns web.primitives.utils
  (:require
   [web.primitives.styles :as styles]
   ["@mui/material/CssBaseline" :default CssBaseline]
   ["@mui/material/styles" :refer [ThemeProvider createTheme]]))

(defn apply-stiles [component]
  [:> ThemeProvider {:theme (createTheme (clj->js styles/theme))}
   [:> CssBaseline]
   component])

(defn handle-paper-props-migration [{:keys [PaperProps paper-props] :as props}]
  (if (map? props)
    (cond->
     (dissoc props
             :PaperProps
             :paper-props)
      (or (some? PaperProps)
          (some? paper-props))
      (assoc-in [:slotProps :paper] (merge PaperProps paper-props)))
    props))

(defn handle-tab-indicator-props-migration [{:keys [TabIndicatorProps tab-indicator-props] :as props}]
  (if (map? props)
    (cond->
     (dissoc props
             :TabIndicatorProps
             :tab-indicator-props)
      (or (some? TabIndicatorProps)
          (some? tab-indicator-props))
      (assoc-in [:slotProps :tabIndicator] (merge TabIndicatorProps tab-indicator-props)))
    props))

(defn handle-root-styles-migration [{:keys [text-align flex-direction align-items overflow
                                            text-overflow white-space display] :as props}]
  (if (map? props)
    (cond->
     (dissoc props
             :display
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
      (some? white-space) (assoc-in [:sx :white-space] white-space)
      (some? display) (assoc-in [:sx :display] display))
    props))

(defn handle-transition-props-migration [{:keys [TransitionProps transition-props] :as props}]
  (if (map? props)
    (cond->
     (dissoc props
             :TransitionProps
             :transition-props)
      (or (some? TransitionProps)
          (some? transition-props))
      (assoc-in [:slotProps :transition] (merge TransitionProps transition-props)))
    props))