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
            ["@mui/material/CircularProgress" :default CircularProgress]
            ["@mui/material/IconButton" :default IconButton]
            ["@mui/material/InputAdornment" :default InputAdornment]
            ["@mui/material/Link" :default Link]
            ["@mui/material/Typography" :default Typography]
            ["@mui/icons-material/ErrorOutlined" :default ErrorOutline]
            ["@mui/icons-material/TaskAlt" :default TaskAlt]
            ["@mui/icons-material/Close" :default CloseIcon]
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
  [:> Grid {:container   true
            :size {:xs 12 :md 4}
            :sx {:align-items "center" :flex-direction "column"}}
   [:> Grid {:size 12}
    button]])

(defn dialog-close-button []
  [:> IconButton {:on-click #(rf/dispatch [::events/close-dialog])
                  :aria-label "close"
                  :sx       {:position "absolute"
                             :right    8
                             :top      8
                             :color    "grey.500"}}
   [:> CloseIcon {}]])

(defn login-dialog-actions [form-type]
  (let [loading? @(rf/subscribe [::subs/loading?])]
    [:> Grid {:container true
              :spacing   2
              :sx        {:padding "16px 16px 10px 16px" :margin "-24px 0 0 0" :width "100%"}}
     (when (some #(= % form-type) [:login])
       [:> Grid {:size {:xs 12 :sm 12}
                 :sx {:text-align "end"}}
        [:> Link {:sx        {:cursor "pointer"}
                  :key       "forgot-password"
                  :on-click  #(rf/dispatch [::events/forgot-password])
                  :color     "primary"
                  :tab-index 3}
         (tr :forgot-password)]])

     [:> Grid {:size 12}
      [:> Grid {:container true
                :spacing   2
                :sx {:width "100%"}}
       (when (some #(= % form-type) [:login])
         [:> Grid {:size {:xs 12 :sm "grow"}}
          [:> Button {:full-width true
                      :variant    "outlined"
                      :color      "primary"
                      :key        "login"
                      :disabled   (or loading?
                                      @(rf/subscribe [::subs/username-invalid?])
                                      @(rf/subscribe [::subs/password-invalid?]))
                      :on-click   #(rf/dispatch [::events/do-login])}
           (if loading?
             [:> CircularProgress {:size 20 :color "inherit"}]
             (tr :login))]])

       (when (some #(= % form-type) [:register])
         [:> Grid {:size {:xs 12 :sm "grow"}}
          [:> Button {:full-width true
                      :variant    "outlined"
                      :key        "register"
                      :disabled   (or loading?
                                      @(rf/subscribe [::subs/username-invalid?])
                                      @(rf/subscribe [::subs/password-invalid?]))
                      :on-click   #(rf/dispatch [::events/do-register])
                      :color      "secondary"}
           (if loading?
             [:> CircularProgress {:size 20 :color "inherit"}]
             (tr :register))]])

       (when (some #(= % form-type) [:confirm-login :confirm-password-reset])
         [:> Grid {:size {:xs 12 :sm 6}}
          [:> Button {:full-width true
                      :variant    "outlined"
                      :key        "resend-code"
                      :disabled   loading?
                      :on-click   #(rf/dispatch [::events/resend-code])
                      :color      "secondary"
                      :tab-index  5}
           (tr :resend-code)]])

       (when (some #(= % form-type) [:confirm-login])
         [:> Grid {:size {:xs 12 :sm "grow"}}
          [:> Button {:full-width true
                      :variant    "outlined"
                      :key        "confirm-login"
                      :disabled   loading?
                      :on-click   #(rf/dispatch [::events/submit-verification])
                      :color      "secondary"}
           (if loading?
             [:> CircularProgress {:size 20 :color "inherit"}]
             (tr :login))]])

       (when (some #(= % form-type) [:confirm-password-reset])
         [:> Grid {:size {:xs 12 :sm "grow"}}
          [:> Button {:full-width true
                      :variant    "outlined"
                      :key        "confirm-password-code"
                      :disabled   (or loading?
                                      @(rf/subscribe [::subs/password-invalid?])
                                      @(rf/subscribe [::subs/confirmation-code-empty?]))
                      :on-click   #(rf/dispatch [::events/confirm-reset-password])
                      :color      "secondary"}
           (if loading?
             [:> CircularProgress {:size 20 :color "inherit"}]
             (tr :confirm-reset-password))]])

       (when (some #(= % form-type) [:forgot-password])
         [:> Grid {:size {:xs 12 :sm "grow"}}
          [:> Button {:full-width true
                      :variant    "outlined"
                      :key        "confirm-login"
                      :disabled   (or loading?
                                      @(rf/subscribe [::subs/username-invalid?]))
                      :on-click   #(rf/dispatch [::events/reset-password])
                      :color      "secondary"}
           (if loading?
             [:> CircularProgress {:size 20 :color "inherit"}]
             (tr :reset-password))]])]]]))

(defn Username []
  (let [loading? @(rf/subscribe [::subs/loading?])]
    [:> TextField {:key           "username"
                   :autoFocus     true
                   :margin        "dense"
                   :default-value ""
                   :disabled      loading?
                   :label         (tr :username)
                   :on-change     #(rf/dispatch [::events/username-change (-> % .-target .-value)])
                   :type          "input"
                   :fullWidth     true
                   :tab-index     0
                   :onKeyUp       #(rf/dispatch [::events/login-on-key-enter %])}]))

(defn password-visibility-icon-button [show-password?]
  (r/as-element [:> InputAdornment {:position "end"}
                 [:> IconButton
                  {:on-click  #(rf/dispatch [::events/toggle-password-visibility])
                   :tab-index 4}
                  (if show-password?
                    [:> VisibilityOff {}]
                    [:> Visibility {}])]]))

(defn password-field []
  (let [show-password? @(rf/subscribe [::subs/show-password?])
        loading?       @(rf/subscribe [::subs/loading?])]
    [:> TextField {:key           "password"
                   :margin        "dense"
                   :default-value ""
                   :disabled      loading?
                   :label         (tr :password)
                   :on-change     #(rf/dispatch [::events/password-change (-> % .-target .-value)])
                   :type          (if show-password? "input" "password")
                   :slotProps     {:input {:endAdornment (password-visibility-icon-button show-password?)}}
                   :fullWidth     true
                   :tab-index     1
                   :onKeyUp       #(rf/dispatch [::events/login-on-key-enter %])}]))

(defn confirm-login []
  (let [loading? @(rf/subscribe [::subs/loading?])]
    [:> TextField {:key           "confirm-login"
                   :margin        "dense"
                   :default-value ""
                   :disabled      loading?
                   :label         (tr :confirmation-code)
                   :on-change     #(rf/dispatch [::events/confirmation-code-change (-> % .-target .-value)])
                   :type          "input"
                   :fullWidth     true
                   :onKeyUp       #(rf/dispatch [::events/login-on-key-enter %])}]))

(defn password-requirement [label state]
  [:> Grid {:container true :spacing 1 :sx {:align-items "center" :width "100%"}}
   [:> Grid {}
    (case state
      :met     [:> TaskAlt {:color "success"}]
      :unmet   [:> ErrorOutline {:color "warning"}]
      :pending [:> TaskAlt {:color "disabled"}])]
   [:> Grid {:size "grow"}
    [:> Typography {:variant "subtitle2"}
     label]]])

(defn password-checks []
  (let [{:keys [missing-upper-case?
                missing-lower-case?
                missing-number?
                missing-length-8?]} @(rf/subscribe [::subs/password-invalid-explanation])

        typed?
        (seq (str @(rf/subscribe [::subs/password])))

        state
        (fn [missing?]
          (cond
            (not typed?) :pending
            missing?     :unmet
            :else        :met))]
    [:> Grid {:container true :spacing 2 :sx {:width "100%"}}
     [:> Grid {:size 12}
      (password-requirement (tr :password-requirements :upper-case) (state missing-upper-case?))]
     [:> Grid {:size 12}
      (password-requirement (tr :password-requirements :lower-case) (state missing-lower-case?))]
     [:> Grid {:size 12}
      (password-requirement (tr :password-requirements :number) (state missing-number?))]
     [:> Grid {:size 12}
      (password-requirement (tr :password-requirements :length) (state missing-length-8?))]]))

(defn login-dialog []
  (let [form-type @(rf/subscribe [::subs/form-type])
        show-error? @(rf/subscribe [::subs/error-message-visible])
        error-message @(rf/subscribe [::subs/error-message])]
    [:> Dialog {:open            @(rf/subscribe [::subs/dialog-visible])
                :maxWidth        "xs"
                :fullWidth       true
                :onClose         #(rf/dispatch [::events/close-dialog])
                :aria-labelledby "form-dialog-title"}
     [:> DialogTitle {:sx {:padding-right "48px"}}
      (tr form-type)
      [dialog-close-button]]
     [:> DialogContent {}
      [:> Grid {:container true :spacing 1 :sx {:flex-direction "column" :width "100%"}}
       (when (some #(= % form-type) [:register :login :forgot-password])
         [:> Grid {:size "grow"}
          [Username]])

       (when (some #(= % form-type) [:register :login :confirm-password-reset])
         [:> Grid {} (password-field)])

       (when (some #(= % form-type) [:register :confirm-password-reset])
         [:> Grid {}
          [:> Grid {:container true :sx {:width "100%"}}
           [:> Grid {}
            (password-checks)]]])

       (when (some #(= % form-type) [:confirm-login :confirm-password-reset])
         [:> Grid {} (confirm-login)])

       (when show-error?
         [:> Grid {}
          [:> Alert {:severity "error"}
           (if (string? error-message)
             error-message
             (str (tr (or error-message :unknown-error))))]])

       (when (and show-error? (= :account-not-confirmed error-message))
         [:> Grid {:sx {:text-align "center"}}
          [:> Link {:sx       {:cursor "pointer"}
                    :on-click #(rf/dispatch [::events/confirm-account])}
           (tr :confirm-account)]])

       (when (= form-type :login)
         [:> Grid {:sx {:text-align "center"}}
          [:> Typography {:variant "body2"}
           (tr :no-account?) " "
           [:> Link {:sx       {:cursor "pointer"}
                     :on-click #(rf/dispatch [::events/open-dialog :register])}
            (tr :create-account)]]])

       (when (= form-type :register)
         [:> Grid {:sx {:text-align "center"}}
          [:> Typography {:variant "body2"}
           (tr :have-account?) " "
           [:> Link {:sx       {:cursor "pointer"}
                     :on-click #(rf/dispatch [::events/open-dialog :login])}
            (tr :go-to-login)]]])]]
     [:> DialogActions (login-dialog-actions form-type)]]))

(defn logout-button []
  [:> Grid {}
   [:> Button {:on-click #(rf/dispatch [::events/logout])}
    (tr :logout)]])

(defn register-button []
  [:> Grid {}
   [:> Button {:on-click #(rf/dispatch [::events/open-dialog :register])}
    (tr :register)]
   (login-dialog)])

(defn login-button []
  [:> Grid {}
   [:> Button {:on-click #(rf/dispatch [::events/open-dialog :login])}
    (tr :login)]])

(defn LoginBar [{:keys [enable-register login-provider]
                 :or   {enable-register true
                        login-provider  :username-password}
                 :as   params}]
  (let [logged? (boolean @(rf/subscribe [::subs/user-name]))
        init? @(rf/subscribe [::subs/init?])]
    (when init?
      [:> Grid {:container       true
                :spacing         3
                :sx {:justify-content :flex-end :width "100%"}}
       (when logged?
         (logout-button))
       (when (and enable-register
                  (false? logged?))
         (register-button))
       (when (false? logged?)
         (login-button))])))






