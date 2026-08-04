(ns web.primitives.layout.core
  (:require
   ["@mui/material/Box" :default Box]
   ["@mui/material/Grid" :default Grid]
   ["@mui/material/Stack" :default Stack]
   [web.primitives.utils :as utils]))

;; MUI v9 Grid only accepts these flex-layout props natively:
;;   container, size, spacing, columnSpacing, rowSpacing, columns,
;;   direction (row | row-reverse only), offset, wrap, sx.
;;
;; Everything else (alignItems, alignContent, justifyContent, etc.) must
;; be passed via sx to avoid React "unrecognized DOM prop" warnings.

(defn- ->sx-layout-props
  "Extract flex layout props that MUI v9 Grid does not support natively and
   return them as a merged sx map.  Handles both kebab-case and camelCase
   caller conventions."
  [props normalized-direction valid-grid-direction?]
  (let [v #(or (get props %1) (get props %2))]
    (cond->
     (or (:sx props) {})
      ;; flex item / container alignment
      (some? (v :align-items :alignItems))      (assoc :alignItems      (v :align-items :alignItems))
      (some? (v :align-content :alignContent))  (assoc :alignContent    (v :align-content :alignContent))
      (some? (v :align-self :alignSelf))        (assoc :alignSelf       (v :align-self :alignSelf))
      (some? (v :justify-content :justifyContent)) (assoc :justifyContent (v :justify-content :justifyContent))
      (some? (v :justify-items :justifyItems))  (assoc :justifyItems    (v :justify-items :justifyItems))
      (some? (v :justify-self :justifySelf))    (assoc :justifySelf     (v :justify-self :justifySelf))
      (some? (v :flex-wrap :flexWrap))          (assoc :flexWrap        (v :flex-wrap :flexWrap))
      ;; column / column-reverse direction — not valid on Grid, use sx
      (and (some? normalized-direction) (not valid-grid-direction?))
      (assoc :flexDirection normalized-direction))))

(def ^:private layout-only-keys
  [:xs :direction
   :align-items :alignItems
   :align-content :alignContent
   :align-self :alignSelf
   :justify-content :justifyContent
   :justify-items :justifyItems
   :justify-self :justifySelf
   :flex-wrap :flexWrap])

(defn RawGrid [{:keys [xs size] :as props} & children]
  (let [size                 (or size (when (some? xs) {:xs xs}))
        direction            (:direction props)
        normalized-direction (when (some? direction)
                               (if (keyword? direction) (name direction) (str direction)))
        valid-grid-direction? (#{"row" "row-reverse"} normalized-direction)
        sx                   (->sx-layout-props props normalized-direction valid-grid-direction?)
        grid-props           (cond->
                              (reduce dissoc props layout-only-keys)
                               (some? size) (assoc :size size)
                               (seq sx)     (assoc :sx sx)
                               valid-grid-direction? (assoc :direction normalized-direction))
        grid-props (utils/handle-root-styles-migration grid-props)]
    (into
     [:> Grid grid-props]
     (for [child children]
       child))))

(defn RawBox [props & children]
  (into
   [:> Box props]
   (for [child children]
     child)))

(defn RawStack [props & children]
  (into
   [:> Stack props]
   (for [child children]
     child)))