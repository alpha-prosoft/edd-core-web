(ns web.primitives.surfaces.core
  (:require
   [re-frame.core :as rf]
   [reagent.core :as r]
   ["@mui/material/index" :refer [AppBar Toolbar Accordion AccordionSummary AccordionDetails
                                  Card CardHeader CardMedia CardContent CardActions
                                  ClickAwayListener CardActionArea
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
   {:expanded  (handle-on-expand-funk expanded? on-expand-func)
    :elevation elevation
    :style     (merge {:width "100%"} style)
    :onChange  on-change}

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

(defn RawHeadlessAccordion [{:keys [id expanded? elevation style details-style]
                             :or   {expanded? true elevation 0 style {}}}
                            content]
  [:> Accordion {:id id
                 :expanded expanded?
                 :elevation elevation
                 :style (merge {:width "100%"} style)}
   [:> AccordionSummary {:style {:display "none"}}]
   (into [:> AccordionDetails
          {:style (merge {:padding "0px"} details-style)}
          content])])

(declare accordion-state-id)

(defn EddAccordion [props content]
  (r/with-let [accordion-state-id (keyword (str "accordion-expanded-state-" (str (random-uuid))))]
    (let [expanded? @(rf/subscribe [::model/get-accordion-expanded-state accordion-state-id props])
          right? (control-position-right? props)
          on-click (fn [] (rf/dispatch [::model/set-accordion-expanded-state accordion-state-id props]))]
      [RawAccordion (merge {:expanded? expanded?
                            :right?    right?
                            :on-click  on-click} props) content])))

(defn RawAppBar [props content]
  [:> AppBar props content])

(defn RawToolbar [props & children]
  (into
   [:> Toolbar props]
   (for [child children]
     child)))

(defn card-action-area [{:keys [header media on-click content-props]
                         :or   {content-props {}}}
                        content]

  [:> CardActionArea {:on-click on-click}
   (when (some? header)
     [:> CardHeader (merge {:titleTypographyProps {:variant "h3"}} header)])
   (when (some? media)
     [:> CardMedia media])
   [:> CardContent content-props content]])

(defn RawCard [{:keys [header media actions on-click elevation content-props action-props]
                :or   {elevation 3 content-props {} action-props {}}
                :as   props} content]
  (let [card-props (dissoc props :on-click :header :media :actions :content-props :action-props)]
    [:> Card
     (merge card-props
            {:elevation elevation})

     (when (and (nil? on-click) (some? header))
       [:> CardHeader (merge {:titleTypographyProps {:variant "h3"}} header)])
     (when (and (nil? on-click) (some? media))
       [:> CardMedia media])

     (if (contains? props :on-click)
       (card-action-area props content)
       [:> CardContent content-props content])

     (when (some? actions)
       [:> CardActions action-props actions])]))

(defn RawClickAwayListener [props content]
  [:> ClickAwayListener props content])
