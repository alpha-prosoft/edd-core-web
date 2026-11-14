(ns projectname.about.views
  (:require [re-frame.core :as rf]
            [projectname.about.elements :as el]
            [projectname.about.subs :as subs]
            [edd.events :as edd-events]
            [web.primitives.components :refer [RawButton RawGrid RawTypography]]))

(defn main-panel
  []
  (let [params
        @(rf/subscribe [::subs/params])

        tab
        (or (:tab params) "overview")]
    [RawGrid {:container true}
     [RawGrid {:size 12}
      [RawTypography {:el-id el/title}
       (str "About item: " (:id params))]]

     [RawGrid {:size 12}
      [RawButton {:el-id    el/overview
                  :on-click #(rf/dispatch [::edd-events/navigate
                                           {:page  :about
                                            :path  {:id (:id params)}
                                            :query {:tab "overview"}}])
                  :variant  (if (= tab "overview") "contained" "outlined")}
       "Overview"]
      [RawButton {:el-id    el/details
                  :on-click #(rf/dispatch [::edd-events/navigate
                                           {:page  :about
                                            :path  {:id (:id params)}
                                            :query {:tab "details"}}])
                  :variant  (if (= tab "details") "contained" "outlined")}
       "Details"]]

     [RawGrid {:size 12}
      (case tab
        "details" [:div "Details content for " (:id params)]
        [:div "Overview content for " (:id params)])]]))
