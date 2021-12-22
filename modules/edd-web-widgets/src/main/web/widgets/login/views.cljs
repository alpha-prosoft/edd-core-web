(ns web.widgets.login.views
  (:require [re-frame.core :as rf]
            [web.widgets.login.subs :as subs]
            [web.widgets.login.events :as events]
            [web.widgets.login.i18n.translate :refer [tr]]

            ["@mui/icons-material/Visibility" :default Visibility]
            ["@mui/icons-material/VisibilityOff" :default VisibilityOff]

            [web.primitives.components
             :refer [RawDialog
                     RawAlert
                     RawButton
                     RawTextField
                     RawIconButton
                     RawGrid]]))

(defn wrap-dialog-button
  [button]
  [RawGrid {:item        true
            :container   true
            :direction   "column"
            :align-items "center"
            :xs          12
            :md          4}
   [RawGrid {:item true
             :xs   12}
    button]])

(defn login-dialog-actions [form-type]
  [RawGrid {:container       true
            :direction       "row"
            :justify-content "space-between"
            :sx              {:padding "0 16px 10px 16px"}}
   (when (some #(= % form-type) [:login])
     [RawGrid {:item true :xs true}
      [RawButton {:key      "forgot-password"
                  :on-click #(rf/dispatch [::events/forgot-password])
                  :color    "primary"}
       (tr :forgot-password)]])

   [RawGrid {:item true :xs true :container true :justify-content "end" :column-spacing 2 :sx {:margin 0}}
    [RawGrid {:item true}
     [RawButton {:key      "cancel"
                 :on-click #(rf/dispatch [::events/close-dialog])
                 :color    "secondary"}
      (tr :cancel)]]
    (when (some #(= % form-type) [:login])
      [RawGrid {:item true}
       [RawButton {:key      "login"
                   :disabled (or @(rf/subscribe [::subs/username-invalid?])
                                 @(rf/subscribe [::subs/password-invalid?]))
                   :on-click #(rf/dispatch [::events/do-login])
                   :color    "secondary"}
        (tr :login)]])
    (when (some #(= % form-type) [:register])
      [RawGrid {:item true}
       [RawButton {:key      "register"
                   :on-click #(rf/dispatch [::events/do-register])
                   :color    "secondary"}
        (tr :register)]])
    (when (some #(= % form-type) [:confirm-login :confirm-password-reset])
      [RawGrid {:item true}
       [RawButton {:key      "resend-code"
                   :on-click #(rf/dispatch [::events/resend-code])
                   :color    "secondary"}
        (tr :resend-code)]])
    (when (some #(= % form-type) [:confirm-login])
      [RawGrid {:item true}
       [RawButton {:key      "confirm-login"
                   :on-click #(rf/dispatch [::events/submit-verification])
                   :color    "secondary"}
        (tr :login)]])
    (when (some #(= % form-type) [:confirm-password-reset])
      [RawGrid {:item true}
       [RawButton {:key      "confirm-password-code"
                   :disabled (or @(rf/subscribe [::subs/password-invalid?])
                                 @(rf/subscribe [::subs/confirmation-code-empty?]))
                   :on-click #(rf/dispatch [::events/confirm-reset-password])
                   :color    "secondary"}
        (tr :confirm-reset-password)]])
    (when (some #(= % form-type) [:forgot-password])
      [RawGrid {:item true}
       [RawButton {:key      "confirm-login"
                   :disabled @(rf/subscribe [::subs/username-invalid?])
                   :on-click #(rf/dispatch [::events/reset-password])
                   :color    "secondary"}
        (tr :reset-password)]])]])

(defn username []
  [RawTextField {:key           "username"
                 :autoFocus     true
                 :margin        "dense"
                 :default-value ""
                 :label         (tr :username)
                 :on-change     #(rf/dispatch [::events/username-change %])
                 :type          "input"
                 :fullWidth     true}])

(defn password []
  (let [show-password? @(rf/subscribe [::subs/show-password?])]
    [RawTextField {:key           "password"
                   :autoFocus     true
                   :margin        "dense"
                   :default-value ""
                   :label         (tr :password)
                   :on-change     #(rf/dispatch [::events/password-change %])
                   :type          (if show-password? "input" "password")
                   :suffix        [RawIconButton
                                   {:on-click #(rf/dispatch [::events/toggle-password-visibility])}
                                   (if show-password?
                                     [:> VisibilityOff {}]
                                     [:> Visibility {}])]
                   :fullWidth     true}]))

(defn confirm-login []
  [RawTextField {:key           "confirm-login"
                 :autoFocus     true
                 :margin        "dense"
                 :default-value ""
                 :label         (tr :confirmation-code)
                 :on-change     #(rf/dispatch [::events/confirmation-code-change %])
                 :type          "input"
                 :fullWidth     true}])

(defn login-dialog []
  (let [form-type @(rf/subscribe [::subs/form-type])
        show-error? @(rf/subscribe [::subs/error-message-visible])]
    [RawDialog {:open            @(rf/subscribe [::subs/dialog-visible])
                :title           (tr form-type)
                :maxWidth        "xs"
                :fullWidth       true
                :onClose         #(rf/dispatch [::events/close-dialog])
                :actions         (login-dialog-actions form-type)
                :aria-labelledby "form-dialog-title"}
     [RawGrid {:container true :direction "column" :spacing 1}
      (when (some #(= % form-type) [:register :login :forgot-password])
        [RawGrid {:item true :xs true}
         (username)])

      (when (some #(= % form-type) [:register :login :confirm-password-reset])
        [RawGrid {:item true} (password)])

      (when (some #(= % form-type) [:confirm-login :confirm-password-reset])
        [RawGrid {:item true} (confirm-login)])

      (when show-error?
        [RawGrid {:item true}
         [RawAlert {:severity "error" :style {:width "100%"}}
          (str (tr @(rf/subscribe [::subs/error-message])))]])]]))

(defn logout-button []
  [RawGrid {:item true}
   [RawButton {:on-click #(rf/dispatch [::events/logout])}
    (tr :logout)]])

(defn register-button []
  [RawGrid {:item true}
   [RawButton {:on-click #(rf/dispatch [::events/open-dialog :register])}
    (tr :register)]
   (login-dialog)])

(defn login-button []
  [RawGrid {:item true}
   [RawButton {:on-click #(rf/dispatch [::events/open-dialog :login])}
    (tr :login)]])

(defn LoginBar [{:keys [] :as params}]
  (let [logged? (boolean @(rf/subscribe [::subs/user-name]))]
    (rf/dispatch [::events/init params])
    [RawGrid {:container true
              :justify   "flex-end"
              :spacing   3}
     (when logged?
       (logout-button))
     (when (false? logged?)
       (register-button))
     (when (false? logged?)
       (login-button))]))







