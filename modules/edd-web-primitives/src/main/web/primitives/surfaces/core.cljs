(ns web.primitives.surfaces.core
  (:require
   [re-frame.core :as rf]
   [reagent.core :as r]
   ["@mui/material/AppBar" :default AppBar]
   ["@mui/material/Toolbar" :default Toolbar]
   ["@mui/material/Accordion" :default Accordion]
   ["@mui/material/AccordionSummary" :default AccordionSummary]
   ["@mui/material/AccordionDetails" :default AccordionDetails]
   ["@mui/material/Card" :default Card]
   ["@mui/material/CardHeader" :default CardHeader]
   ["@mui/material/CardContent" :default CardContent]
   ["@mui/material/CardMedia" :default CardMedia]
   ["@mui/material/CardActions" :default CardActions]
   ["@mui/material/CardActionArea" :default CardActionArea]
   ["@mui/material/ClickAwayListener" :default ClickAwayListener]
   ["@mui/material/Grid" :default Grid]

   [web.primitives.icons.core :refer [ExpandMoreIcon ExpandLessIcon]]

   [web.primitives.surfaces.model :as model]))

(defn- control-position-right? [props]
  (let [control-position (:control-position props)]
    (and (some? control-position) (= control-position :right))))

(defn handle-on-expand-funk [expanded? on-expand-func]
  (when (and (some? on-expand-func) expanded?) (on-expand-func))
  expanded?)

(defn RawAccordion [{:keys [expanded? control-position  on-click on-change on-expand-func
                            elevation  disabled
                            style sx
                            control-sx
                            summary-style summary-sx
                            summary-class-name
                            details-style details-sx]
                     :or   {control-position :left elevation 0 disabled false control-sx {}}
                     :as   props} content]
  [:> Accordion
   {:expanded  (handle-on-expand-funk expanded? on-expand-func)
    :elevation elevation
    :sx        (merge
                {:width "100%"}
                (or sx style))
    :onChange  on-change}

   [:> AccordionSummary
    (merge {:sx        (merge
                        {}
                        (or summary-sx summary-style))
            :disabled   disabled
            :on-click   on-click
            :children  (r/as-element [:> Grid {:container true :size 12 :sx {:alignItems "center"}}
                                      [:> Grid {:sx (merge
                                                     {:position "absolute" :top "0px"}
                                                     (when (= control-position :right) {:right "0px"})
                                                     control-sx)}
                                       [:> Grid
                                        (merge
                                         {:sx (if (= control-position :right)
                                                {:marginRight "-1.1rem"}
                                                {:marginLeft "-1.1rem"})}
                                         (when (:id props) {:id (:id props)}))
                                        (if expanded? [ExpandLessIcon {}] [ExpandMoreIcon {}])]]
                                      [:> Grid {:size 12 :sx (if (= control-position :right)
                                                               {:paddingRight "2.5rem"}
                                                               {:paddingLeft "2.5rem"})}
                                       (if (and expanded? (contains? props :header-expanded))
                                         (:header-expanded props)
                                         (:header props))]])}
           (when (some? summary-class-name) {:class-name summary-class-name}))]
   [:> AccordionDetails
    {:sx (merge {:padding "0px"} (or details-sx details-style))}
    content]])

(defn RawHeadlessAccordion [{:keys [id expanded? elevation sx styles details-sx details-styles]
                             :or   {expanded? true elevation 0 sx {}}}
                            content]
  [:> Accordion {:id        id
                 :expanded  expanded?
                 :elevation elevation
                 :sx        (merge {:width "100%"} (or sx styles))}
   [:> AccordionSummary {:sx {:display "none"}}]
   (into
    [:> AccordionDetails
     {:sx (merge {:padding "0px"} (or details-sx details-styles))}
     content])])

(declare accordion-state-id)

(defn EddAccordion [props content]
  (r/with-let [uuid (str (random-uuid))
               accordion-state-id (keyword (str "accordion-expanded-state-" uuid))]
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
     [:> CardHeader (merge {:slotProps {:title {:variant "h3"}}} header)])
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
       [:> CardHeader (merge {:slotProps {:title {:variant "h3"}}} header)])
     (when (and (nil? on-click) (some? media))
       [:> CardMedia media])

     (if (contains? props :on-click)
       (card-action-area props content)
       [:> CardContent content-props content])

     (when (some? actions)
       [:> CardActions action-props actions])]))

(defn RawClickAwayListener [props content]
  [:> ClickAwayListener props content])
