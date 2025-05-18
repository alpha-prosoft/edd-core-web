(ns web.devcards.confirmation-dialog
  (:require
    [cljsjs.react]
    [cljsjs.react.dom]
    [devcards.core :refer-macros (defcard-rg)]
    [re-frame.core :as rf]
    [web.widgets.confirmation-dialog.views :as views]
    [web.widgets.confirmation-dialog.events :as events]

    ["@mui/material/Button" :default Button]
    ["@mui/material/Grid" :default Grid]))


(defcard-rg :confirmation-dialog
            "## Confirmation Dialog"
            (fn []
              [:> Grid {:container true :align-items "center"}
               [:> Grid {:item true :xs 4}
                [:> Button {:on-click #(rf/dispatch [::events/open-confirmation-dialog
                                                     {:message         [:> Grid {:container true
                                                                                 :sx        {:width "550px"}} "Are you sure?"]
                                                      :proceed-text    "Yes, proceed"
                                                      :cancel-text     "Nope, cancel"
                                                      :on-proceed-func (fn [] (print "on-proceed"))
                                                      :on-cancel-func  (fn [] (print "on-cancel"))}])}
                 "Open dialog"]]
               (views/ConfirmationDialog {:button-props          {:variant "outlined"
                                                          :full-width      true
                                                          :color           "secondary"}
                                           :proceed-button-props {:sx {:background "#94c4a8"
                                                                       "&:hover" {:background "#126216"
                                                                                  :color "#fff"}}}})]))