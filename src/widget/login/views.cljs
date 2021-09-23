(ns widget.login.views
  (:require [re-frame.core :as rf]
            [widget.login.subs :as subs]
            [widget.login.events :as events]
            [edd.subs :as edd-subs]
            [edd.events :as edd-events]
            [edd.i18n :refer [tr]]

            ["@material-ui/core/Dialog" :default Dialog]
            ["@material-ui/core/DialogActions" :default DialogActions]
            ["@material-ui/core/DialogContent" :default DialogContent]
            ["@material-ui/core/DialogTitle" :default DialogTitle]
            ["@material-ui/core/Snackbar" :default Snackbar]
            ["@material-ui/lab/Alert" :default Alert]

            ["@material-ui/core/Button" :default Button]
            ["@material-ui/core/TextField" :default TextField]
            ["@material-ui/core/Grid" :default Grid]
            ["@material-ui/core/Select" :default Select]
            ["@material-ui/core/MenuItem" :default MenuItem]
            ["@material-ui/core/FormControl" :default FormControl]))

(defn wrap-dialog-button
  [button]
  [:> Grid {:item        true
            :container   true
            :direction   "column"
            :align-items "center"
            :xs          12
            :md          4}
   [:> Grid {:item true
             :xs   12}
    button]])

(defn language-item
  []
  (let [show? @(rf/subscribe [::edd-subs/show-language-switcher?])]
    (when show?
      [:> Grid {:item true :xs false}
       [:> FormControl
        [:> Select {:value     @(rf/subscribe [::edd-subs/selected-language])
                    :on-change #(rf/dispatch [::edd-events/change-language (-> (.-target %) (.-value) (keyword))])}
         [:> MenuItem {:value :en} "English"]
         [:> MenuItem {:value :de} "Deutsch"]]]])))

(defn LoginDialog
  [props]
  (let [form-type @(rf/subscribe [::subs/form-type])]
    [:> Dialog {:open            @(rf/subscribe [::subs/dialog-visible])
                :maxWidth        "xs"
                :fullWidth       true
                :onClose         #(rf/dispatch [::events/close-dialog])
                :aria-labelledby "form-dialog-title"}
     [:> DialogTitle (tr form-type)]

     [:> DialogContent
      [:> Grid {:container true}
       (if (some #(= % form-type) [:register :login :forgot-password])
         [:> TextField {:key           "username"
                        :autoFocus     true
                        :margin        "dense"
                        :default-value ""
                        :label         (tr :username)
                        :on-change     #(rf/dispatch [::events/username-change (-> % .-target .-value)])
                        :type          "input"
                        :fullWidth     true}])

       (if (some #(= % form-type) [:register :login :confirm-password-reset])
         [:> TextField {:key           "password"
                        :autoFocus     true
                        :margin        "dense"
                        :default-value ""
                        :label         (tr :password)
                        :on-change     #(rf/dispatch [::events/password-change (-> % .-target .-value)])
                        :type          "password"
                        :fullWidth     true}])

       (if (some #(= % form-type) [:confirm-login :confirm-password-reset])
         [:> TextField {:key           "confirm-login"
                        :autoFocus     true
                        :margin        "dense"
                        :default-value ""
                        :label         (tr :confirmation-code)
                        :on-change     #(rf/dispatch [::events/confirmation-code-change (-> % .-target .-value)])
                        :type          "input"
                        :fullWidth     true}])]
      (if @(rf/subscribe [::subs/error-message-visible])
        [:> Alert {:severity "error"}
         (str (tr @(rf/subscribe [::subs/error-message])))])

      [:> DialogActions
       [:> Grid {:container  true
                 :direction  "row"
                 :justify    "flex-end"
                 :alignItems "center"}
        (if (some #(= % form-type) [:login])
          (wrap-dialog-button [:> Button {:key      "forgot-password"
                                          :on-click #(rf/dispatch [::events/forgot-password])
                                          :color    "primary"} (tr :forgot-password)]))

        (wrap-dialog-button
          [:> Button {:key      "cancel"
                      :on-click #(rf/dispatch [::events/close-dialog])
                      :color    "secondary"} (tr :cancel)])
        (if (some #(= % form-type) [:login])
          (wrap-dialog-button
            [:> Button {:key      "login"
                        :on-click #(rf/dispatch [::events/do-login])
                        :color    "secondary"} (tr :login)]))
        (if (some #(= % form-type) [:register])
          (wrap-dialog-button
            [:> Button {:key      "register"
                        :on-click #(rf/dispatch [::events/do-register])
                        :color    "secondary"} (tr :register)]))

        (if (some #(= % form-type) [:confirm-login :confirm-password-reset])
          (wrap-dialog-button
           [:> Button {:key      "resend-code"
                       :on-click #(rf/dispatch [::events/resend-code])
                       :color    "secondary"} (tr :resend-code)]))

        (if (some #(= % form-type) [:confirm-login])
          (wrap-dialog-button
           [:> Button {:key      "confirm-login"
                       :on-click #(rf/dispatch [::events/submit-verification])
                       :color    "secondary"} (tr :login)]))

        (if (some #(= % form-type) [:confirm-password-reset])
          (wrap-dialog-button
           [:> Button {:key      "confirm-password-code"
                       :on-click #(rf/dispatch [::events/confirm-reset-password])
                       :color    "secondary"} (tr :confirm-reset-password)]))

        (if (some #(= % form-type) [:forgot-password])
          (wrap-dialog-button
           [:> Button {:key      "confirm-login"
                       :on-click #(rf/dispatch [::events/reset-password])
                       :color    "secondary"} (tr :reset-password)]))]]]]))

(defn logout-button
  []
  [:> Grid {:item true}
   [:> Button {:on-click #(rf/dispatch [::events/logout])}
    (tr :logout)]])

(defn register-button
  []
  [:> Grid {:item true}
   [:> Button {:on-click #(rf/dispatch [::events/open-dialog :register])}
    (tr :register)]
   (LoginDialog {})])

(defn login-button
  []
  [:> Grid {:item true}
   [:> Button {:on-click #(rf/dispatch [::events/open-dialog :login])}
    (tr :login)]])

(defn LoginBar
  []
  (let [logged? (boolean @(rf/subscribe [::subs/user-name]))]
    [:> Grid {:container true
              :justify   "flex-end"
              :spacing   3}
     (language-item)
     (when logged?
       logout-button)
     (when (false? logged?)
       (register-button))
     (when (false? logged?)
       (login-button))]))







