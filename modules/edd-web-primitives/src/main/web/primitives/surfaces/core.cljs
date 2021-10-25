(ns web.primitives.surfaces.core
  (:require
   [re-frame.core :as rf]
   [reagent.core :as r]
   ["@mui/material/index" :refer [Accordion AccordionSummary AccordionDetails
                                  Card CardHeader CardMedia CardContent CardActions
                                  ClickAwayListener
                                  Grid
                                  IconButton]]
   [web.primitives.icons.core :refer [ExpandMoreIcon ExpandLessIcon]]

   [web.primitives.surfaces.model :as model]))

(defn- control-position-right? [props]
  (let [control-position (:control-position props)]
    (and (some? control-position) (= control-position :right))))

(defn handle-on-expand-funk [expanded? on-expand-func]
  (when (and (some? on-expand-func) expanded?) (on-expand-func))
  expanded?)

(defn RawAccordion [{:keys [expanded? control-position on-click on-change on-expand-func
                            elevation style disabled summary-style summary-class-name details-style]
                     :or   {control-position :left elevation 0 disabled false}
                     :as   props} content]
  [:> Accordion
   (merge
    {:expanded  (handle-on-expand-funk expanded? on-expand-func)
     :elevation elevation
     :style     (merge {:width "100%"} style)
     :onChange  on-change})

   [:> AccordionSummary
    (merge {:style (merge {} summary-style)}
           (when (some? summary-class-name) {:class-name summary-class-name}))
    [:> Grid {:container true :alignItems "center"}
     [:> Grid {:item  true
               :style (merge
                       {:position "absolute" :top "0px"}
                       (when (= control-position :right) {:right "0px"}))}
      [:> IconButton
       (merge
        {:on-click on-click
         :disabled disabled
         :style    (if (= control-position :right)
                     {:marginRight "-1.1rem"}
                     {:marginLeft "-1.1rem"})}
        (when (:id props) {:id (:id props)}))
       (if expanded? [ExpandLessIcon {}] [ExpandMoreIcon {}])]]
     [:> Grid {:container true :style (if (= control-position :right)
                                        {:paddingRight "2.5rem"}
                                        {:paddingLeft "2.5rem"})}
      (if (and expanded? (contains? props :header-expanded))
        (:header-expanded props)
        (:header props))]]]
   (into [:> AccordionDetails
          {:style (merge {:padding "0px"} details-style)}
          content])])

(defn EddAccordion [props content]
  (r/with-let [accordion-state-id (keyword (str "accordion-expanded-state-" (str (random-uuid))))]
    (let [expanded? @(rf/subscribe [::model/get-accordion-expanded-state accordion-state-id props])
          right? (control-position-right? props)
          on-click (fn [] (rf/dispatch [::model/set-accordion-expanded-state accordion-state-id props]))]
      [RawAccordion (merge {:expanded? expanded?
                            :right?    right?
                            :on-click  on-click} props) content])))

(defn RawCard [{:keys [header media actions elevation content-props action-props]
                :or   {elevation 3 content-props {} action-props {}}
                :as   props} content]
  (let [card-props (dissoc props :header :media :actions :content-props :action-props)]
    [:> Card
     (merge card-props
            {:elevation elevation})
     (when (some? header)
       [:> CardHeader (merge {:titleTypographyProps {:variant "h3"}} header)])
     (when (some? media)
       [:> CardMedia media])
     [:> CardContent content-props content]
     (when (some? actions)
       [:> CardActions action-props actions])]))

(defn RawClickAwayListener [props content]
  [:> ClickAwayListener props content])
