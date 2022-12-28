(ns web.widgets.login.views
  (:require [re-frame.core :as rf]
            [web.widgets.login.subs :as subs]
            [web.widgets.login.events :as events]
            [web.widgets.login.i18n.translate :refer [tr]]

            ["@mui/icons-material/Visibility" :default Visibility]
            ["@mui/icons-material/VisibilityOff" :default VisibilityOff]
            ["@mui/material/Grid" :default Grid]
            ["@mui/material/Button" :default Button]
            ["@mui/material/Dialog" :default Dialog]
            ["@mui/material/DialogTitle" :default DialogTitle]
            ["@mui/material/DialogContent" :default DialogContent]
            ["@mui/material/DialogActions" :default DialogActions]
            ["@mui/material/TextField" :default TextField]
            ["@mui/material/Alert" :default Alert]
            ["@mui/material/IconButton" :default IconButton]
            ["@mui/material/InputAdornment" :default InputAdornment]
            ["@mui/material/Link" :default Link]
            [reagent.core :as r]))

(defn login-on-key-enter [event]
  (let [key-enter? (= (.-key event) "Enter")
        form-type @(rf/subscribe [::subs/form-type])]
    (when key-enter?
      (when (and (= form-type :login)
                 (not @(rf/subscribe [::subs/username-invalid?]))
                 (not @(rf/subscribe [::subs/password-invalid?])))
        (rf/dispatch [::events/do-login]))

      (when (and (= form-type :register)
                 (not @(rf/subscribe [::subs/username-invalid?]))
                 (not @(rf/subscribe [::subs/password-invalid?])))
        (rf/dispatch [::events/do-register]))

      (when (and (= form-type :confirm-password-reset)
                 (not @(rf/subscribe [::subs/password-invalid?]))
                 (not @(rf/subscribe [::subs/confirmation-code-empty?])))
        (rf/dispatch [::events/confirm-reset-password]))

      (when (and (= form-type :forgot-password)
                 (not @(rf/subscribe [::subs/username-invalid?])))
        (rf/dispatch [::events/reset-password])))))

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

(defn login-dialog-actions [form-type]
  [:> Grid {:container true
            :spacing   2
            :sx        {:padding "0 16px 10px 0" :margin "-24px 0 0 0"}}
   (when (some #(= % form-type) [:login])
     [:> Grid {:item       true
               :xs         12
               :sm         12
               :text-align "end"}
      [:> Link {:sx        {:cursor "pointer"}
                :key       "forgot-password"
                :on-click  #(rf/dispatch [::events/forgot-password])
                :color     "primary"
                :tab-index 3}
       (tr :forgot-password)]])

   [:> Grid {:item true
             :xs   12}
    [:> Grid {:container true
              :spacing   2}
     [:> Grid {:item true :sm true :xs 12}
      [:> Button {:full-width true
                  :variant    "outlined"
                  :key        "cancel"
                  :on-click   #(rf/dispatch [::events/close-dialog])
                  :color      "secondary"
                  :tab-index  4}
       (tr :cancel)]]

     (when (some #(= % form-type) [:login])
       [:> Grid {:item true :sm true :xs 12}
        [:> Button {:full-width true
                    :variant    "outlined"
                    :key        "login"
                    :disabled   (or @(rf/subscribe [::subs/username-invalid?])
                                    @(rf/subscribe [::subs/password-invalid?]))
                    :on-click   #(rf/dispatch [::events/do-login])
                    :color      "secondary"}
         (tr :login)]])

     (when (some #(= % form-type) [:register])
       [:> Grid {:item true :sm true :xs 12}
        [:> Button {:full-width true
                    :variant    "outlined"
                    :key        "register"
                    :disabled   (or @(rf/subscribe [::subs/username-invalid?])
                                    @(rf/subscribe [::subs/password-invalid?]))
                    :on-click   #(rf/dispatch [::events/do-register])
                    :color      "secondary"}
         (tr :register)]])

     (when (some #(= % form-type) [:confirm-login :confirm-password-reset])
       [:> Grid {:item true :sm 6 :xs 12}
        [:> Button {:full-width true
                    :variant    "outlined"
                    :key        "resend-code"
                    :on-click   #(rf/dispatch [::events/resend-code])
                    :color      "secondary"
                    :tab-index  5}
         (tr :resend-code)]])

     (when (some #(= % form-type) [:confirm-login])
       [:> Grid {:item true :sm true :xs 12}
        [:> Button {:full-width true
                    :variant    "outlined"
                    :key        "confirm-login"
                    :on-click   #(rf/dispatch [::events/submit-verification])
                    :color      "secondary"}
         (tr :login)]])

     (when (some #(= % form-type) [:confirm-password-reset])
       [:> Grid {:item true :sm true :xs 12}
        [:> Button {:full-width true
                    :variant    "outlined"
                    :key        "confirm-password-code"
                    :disabled   (or @(rf/subscribe [::subs/password-invalid?])
                                    @(rf/subscribe [::subs/confirmation-code-empty?]))
                    :on-click   #(rf/dispatch [::events/confirm-reset-password])
                    :color      "secondary"}
         (tr :confirm-reset-password)]])

     (when (some #(= % form-type) [:forgot-password])
       [:> Grid {:item true :sm true :xs 12}
        [:> Button {:full-width true
                    :variant    "outlined"
                    :key        "confirm-login"
                    :disabled   @(rf/subscribe [::subs/username-invalid?])
                    :on-click   #(rf/dispatch [::events/reset-password])
                    :color      "secondary"}
         (tr :reset-password)]])]]])

(defn username []
  [:> TextField {:key           "username"
                 :autoFocus     true
                 :margin        "dense"
                 :default-value ""
                 :label         (tr :username)
                 :on-change     #(rf/dispatch [::events/username-change (-> % .-target .-value)])
                 :type          "input"
                 :fullWidth     true
                 :tab-index     0
                 :onKeyUp       #(login-on-key-enter %)}])

(defn password-visibility-icon-button [show-password?]
  (r/as-element [:> InputAdornment {:position "end"}
                 [:> IconButton
                  {:on-click  #(rf/dispatch [::events/toggle-password-visibility])
                   :tab-index 4}
                  (if show-password?
                    [:> VisibilityOff {}]
                    [:> Visibility {}])]]))

(defn password []
  (let [show-password? @(rf/subscribe [::subs/show-password?])]
    [:> TextField {:key           "password"
                   :margin        "dense"
                   :default-value ""
                   :label         (tr :password)
                   :on-change     #(rf/dispatch [::events/password-change (-> % .-target .-value)])
                   :type          (if show-password? "input" "password")
                   :InputProps    {:endAdornment (password-visibility-icon-button show-password?)}
                   :fullWidth     true
                   :tab-index     1
                   :onKeyUp       #(login-on-key-enter %)}]))

(defn confirm-login []
  [:> TextField {:key           "confirm-login"
                 :margin        "dense"
                 :default-value ""
                 :label         (tr :confirmation-code)
                 :on-change     #(rf/dispatch [::events/confirmation-code-change (-> % .-target .-value)])
                 :type          "input"
                 :fullWidth     true
                 :onKeyUp       #(login-on-key-enter %)}])

(defn login-dialog []
  (let [form-type @(rf/subscribe [::subs/form-type])
        show-error? @(rf/subscribe [::subs/error-message-visible])
        error-message @(rf/subscribe [::subs/error-message])]
    [:> Dialog {:open            @(rf/subscribe [::subs/dialog-visible])
                :maxWidth        "xs"
                :fullWidth       true
                :onClose         #(rf/dispatch [::events/close-dialog])
                :aria-labelledby "form-dialog-title"}
     [:> DialogTitle (tr form-type)]
     [:> DialogContent {}
      [:> Grid {:container true :direction "column" :spacing 1}
       (when (some #(= % form-type) [:register :login :forgot-password])
         [:> Grid {:item true :xs true}
          (username)])

       (when (some #(= % form-type) [:register :login :confirm-password-reset])
         [:> Grid {:item true} (password)])

       (when (some #(= % form-type) [:confirm-login :confirm-password-reset])
         [:> Grid {:item true} (confirm-login)])

       (when show-error?
         [:> Grid {:item true}
          [:> Alert {:severity "error"}
           (str (tr (or error-message :unknown-error)))]])]]
     [:> DialogActions (login-dialog-actions form-type)]]))

(defn logout-button []
  [:> Grid {:item true}
   [:> Button {:on-click #(rf/dispatch [::events/logout])}
    (tr :logout)]])

(defn register-button []
  [:> Grid {:item true}
   [:> Button {:on-click #(rf/dispatch [::events/open-dialog :register])}
    (tr :register)]
   (login-dialog)])

(defn login-button []
  [:> Grid {:item true}
   [:> Button {:on-click #(rf/dispatch [::events/open-dialog :login])}
    (tr :login)]])

(defn LoginBar [{:keys [enable-register login-provider]
                 :or {enable-register true
                      login-provider :username-password}
                 :as params}]
  (let [logged? (boolean @(rf/subscribe [::subs/user-name]))
        init? @(rf/subscribe [::subs/init?])]
    (rf/dispatch [::events/init params])
    (when init?
      [:> Grid {:container       true
                :justify-content :flex-end
                :spacing         3}
       (when logged?
         (logout-button))
       (when (and enable-register
                  (false? logged?))
         (register-button))
       (when (false? logged?)
         (login-button))])))






