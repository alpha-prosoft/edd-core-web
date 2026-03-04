(ns projectname.about.views
  (:require [re-frame.core :as rf]
            [projectname.about.subs :as subs]
            [edd.events :as edd-events]
            ["@mui/material/Grid" :default Grid]
            ["@mui/material/Button" :default Button]))

(defn main-panel
  [_classes]
  (let [params
        @(rf/subscribe [::subs/params])

        tab
        (or (:tab params) "overview")]
    [:> Grid {:container true :item true}
     [:> Grid {:item true :xs 12}
      (str "About item: " (:id params))]

     [:> Grid {:item true :xs 12}
      [:> Button {:on-click #(rf/dispatch [::edd-events/navigate
                                           {:page  :about
                                            :path  {:id (:id params)}
                                            :query {:tab "overview"}}])
                  :variant  (if (= tab "overview") "contained" "outlined")}
       "Overview"]
      [:> Button {:on-click #(rf/dispatch [::edd-events/navigate
                                           {:page  :about
                                            :path  {:id (:id params)}
                                            :query {:tab "details"}}])
                  :variant  (if (= tab "details") "contained" "outlined")}
       "Details"]]

     [:> Grid {:item true :xs 12}
      (case tab
        "details" [:div "Details content for " (:id params)]
        [:div "Overview content for " (:id params)])]]))
