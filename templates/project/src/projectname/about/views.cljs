(ns projectname.about.views
  (:require [re-frame.core :as rf]
            [projectname.about.subs :as subs]
            [edd.events :as edd-events]
            [web.primitives.components :refer [RawButton RawGrid]]))

(defn main-panel
  [_classes]
  (let [params
        @(rf/subscribe [::subs/params])

        tab
        (or (:tab params) "overview")]
    [RawGrid {:container true}
     [RawGrid {:size 12}
      (str "About item: " (:id params))]

     [RawGrid {:size 12}
      [RawButton {:on-click #(rf/dispatch [::edd-events/navigate
                                           {:page  :about
                                            :path  {:id (:id params)}
                                            :query {:tab "overview"}}])
                  :variant  (if (= tab "overview") "contained" "outlined")}
       "Overview"]
      [RawButton {:on-click #(rf/dispatch [::edd-events/navigate
                                           {:page  :about
                                            :path  {:id (:id params)}
                                            :query {:tab "details"}}])
                  :variant  (if (= tab "details") "contained" "outlined")}
       "Details"]]

     [RawGrid {:size 12}
      (case tab
        "details" [:div "Details content for " (:id params)]
        [:div "Overview content for " (:id params)])]]))
