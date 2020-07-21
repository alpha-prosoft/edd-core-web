(ns web.primitives.surfaces.core
  (:require
   [re-frame.core :as rf]
   [reagent.core :as r]
   ["@material-ui/core" :refer [Accordion AccordionSummary AccordionDetails
                                Card CardHeader CardMedia CardContent CardActions
                                ClickAwayListener
                                Grid
                                IconButton]]
   ["@material-ui/icons/ExpandMore" :default ExpandMoreIcon]
   ["@material-ui/icons/ExpandLess" :default ExpandLessIcon]

   [web.primitives.surfaces.model :as model]))

(defn- control-position-right? [props]
  (let [control-position (:control-position props)]
    (and (some? control-position) (= control-position :right))))

(defn handle-on-expand-funk [expanded? on-expand-func]
  (when (and (some? on-expand-func) expanded?) (on-expand-func))
  expanded?)

(defn RawAccordion [{:keys [expanded? control-position on-click on-change on-expand-func
                            elevation style disabled summary-style summary-class-name details-style]
                     :or {control-position :left elevation 0 disabled false}
                     :as props} content]
  [:> Accordion
   (merge
    {:expanded (handle-on-expand-funk expanded? on-expand-func)
     :elevation elevation
     :style    (merge {:width "100%"} style)
     :onChange on-change})

   [:> AccordionSummary
    (merge {:style (merge {} summary-style)}
           (when (some? summary-class-name) {:class-name summary-class-name}))
    [:> Grid {:container true :alignItems "center"}
     [:> Grid {:item true
               :style (merge
                       {:position "absolute" :top 0}
                       (if (= control-position :right) {:right 0}))}
      [:> IconButton
       (merge
        {:on-click on-click
         :disabled disabled
         :style (if (= control-position :right)
                  {:margin-right "-1.1rem"}
                  {:margin-left "-1.1rem"})}
        (if (:id props) {:id (:id props)}))
       (if expanded? [:> ExpandLessIcon] [:> ExpandMoreIcon])]]
     [:> Grid {:container true :style (if (= control-position :right)
                                        {:padding-right "2.5rem"}
                                        {:padding-left "2.5rem"})}
      (if (and expanded? (contains? props :header-expanded))
        (:header-expanded props)
        (:header props))]]]
   (into [:> AccordionDetails
          {:style (merge {:padding 0} details-style)}
          content])])

(defn EddAccordion [props content]
  (r/with-let [accordion-state-id (keyword (str "accordion-expanded-state-" (str (random-uuid))))]
    (let [expanded? @(rf/subscribe [::model/get-accordion-expanded-state accordion-state-id props])
          right? (control-position-right? props)
          on-click (fn [] (rf/dispatch [::model/set-accordion-expanded-state accordion-state-id props]))]
      [RawAccordion (merge {:expanded? expanded?
                            :right? right?
                            :on-click on-click} props) content])))

(defn RawCard [{:keys [header media actions elevation content-props action-props]
                :or {elevation 3 content-props {} action-props {}}
                :as props} content]
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
