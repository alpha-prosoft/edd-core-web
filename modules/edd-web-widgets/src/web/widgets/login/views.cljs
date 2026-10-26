(ns web.widgets.login.views
  (:require [re-frame.core :as rf]
            [web.widgets.login.subs :as subs]
            [web.widgets.login.events :as events]
            [web.widgets.login.i18n.translate :refer [tr]]
            [web.primitives.components :refer [RawAlert RawButton RawCircularProgress RawDialog
                                               RawGrid RawIconButton RawLink RawTextField
                                               RawTypography]]
            [web.primitives.icons.core :refer [CloseIcon ErrorOutlineIcon TaskAltIcon
                                               VisibilityIcon VisibilityOffIcon]]))

(def ^:private el :login-bar)

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
  [RawGrid {:container   true
            :size {:xs 12 :md 4}
            :sx {:align-items "center" :flex-direction "column"}}
   [RawGrid {:size 12}
    button]])

(defn dialog-close-button []
  [RawIconButton {:el-id      [el :close]
                  :on-click   #(rf/dispatch [::events/close-dialog])
                  :aria-label "close"
                  :sx         {:position "absolute"
                               :right    8
                               :top      8
                               :color    "grey.500"}}
   [CloseIcon]])

(defn login-dialog-actions [form-type]
  (let [loading? @(rf/subscribe [::subs/loading?])]
    [RawGrid {:container true
              :spacing   2
              :sx        {:padding "16px 16px 10px 16px" :margin "-24px 0 0 0" :width "100%"}}
     (when (some #(= % form-type) [:login])
       [RawGrid {:size {:xs 12 :sm 12}
                 :sx {:text-align "end"}}
        [RawLink {:sx        {:cursor "pointer"}
                  :el-id     [el :forgot-password]
                  :key       "forgot-password"
                  :on-click  #(rf/dispatch [::events/forgot-password])
                  :color     "primary"
                  :tab-index 3}
         (tr :forgot-password)]])

     [RawGrid {:size 12}
      [RawGrid {:container true
                :spacing   2
                :sx {:width "100%"}}
       (when (some #(= % form-type) [:login])
         [RawGrid {:size {:xs 12 :sm "grow"}}
          [RawButton {:full-width true
                      :variant    "outlined"
                      :color      "primary"
                      :el-id      [el :submit-login]
                      :key        "login"
                      :disabled   (or loading?
                                      @(rf/subscribe [::subs/username-invalid?])
                                      @(rf/subscribe [::subs/password-invalid?]))
                      :on-click   #(rf/dispatch [::events/do-login])}
           (if loading?
             [RawCircularProgress {:size 20 :color "inherit"}]
             (tr :login))]])

       (when (some #(= % form-type) [:register])
         [RawGrid {:size {:xs 12 :sm "grow"}}
          [RawButton {:full-width true
                      :variant    "outlined"
                      :el-id      [el :submit-register]
                      :key        "register"
                      :disabled   (or loading?
                                      @(rf/subscribe [::subs/username-invalid?])
                                      @(rf/subscribe [::subs/password-invalid?]))
                      :on-click   #(rf/dispatch [::events/do-register])
                      :color      "secondary"}
           (if loading?
             [RawCircularProgress {:size 20 :color "inherit"}]
             (tr :register))]])

       (when (some #(= % form-type) [:confirm-login :confirm-password-reset])
         [RawGrid {:size {:xs 12 :sm 6}}
          [RawButton {:full-width true
                      :variant    "outlined"
                      :el-id      [el :resend-code]
                      :key        "resend-code"
                      :disabled   loading?
                      :on-click   #(rf/dispatch [::events/resend-code])
                      :color      "secondary"
                      :tab-index  5}
           (tr :resend-code)]])

       (when (some #(= % form-type) [:confirm-login])
         [RawGrid {:size {:xs 12 :sm "grow"}}
          [RawButton {:full-width true
                      :variant    "outlined"
                      :el-id      [el :submit-verification]
                      :key        "confirm-login"
                      :disabled   loading?
                      :on-click   #(rf/dispatch [::events/submit-verification])
                      :color      "secondary"}
           (if loading?
             [RawCircularProgress {:size 20 :color "inherit"}]
             (tr :login))]])

       (when (some #(= % form-type) [:confirm-password-reset])
         [RawGrid {:size {:xs 12 :sm "grow"}}
          [RawButton {:full-width true
                      :variant    "outlined"
                      :el-id      [el :submit-password-reset]
                      :key        "confirm-password-code"
                      :disabled   (or loading?
                                      @(rf/subscribe [::subs/password-invalid?])
                                      @(rf/subscribe [::subs/confirmation-code-empty?]))
                      :on-click   #(rf/dispatch [::events/confirm-reset-password])
                      :color      "secondary"}
           (if loading?
             [RawCircularProgress {:size 20 :color "inherit"}]
             (tr :confirm-reset-password))]])

       (when (some #(= % form-type) [:forgot-password])
         [RawGrid {:size {:xs 12 :sm "grow"}}
          [RawButton {:full-width true
                      :variant    "outlined"
                      :el-id      [el :submit-reset-password]
                      :key        "confirm-login"
                      :disabled   (or loading?
                                      @(rf/subscribe [::subs/username-invalid?]))
                      :on-click   #(rf/dispatch [::events/reset-password])
                      :color      "secondary"}
           (if loading?
             [RawCircularProgress {:size 20 :color "inherit"}]
             (tr :reset-password))]])]]]))

(defn Username []
  (let [loading? @(rf/subscribe [::subs/loading?])]
    [RawTextField {:variant       "outlined"
                   :el-id         [el :username]
                   :key           "username"
                   :autoFocus     true
                   :margin        "dense"
                   :default-value ""
                   :disabled      loading?
                   :label         (tr :username)
                   :on-change     #(rf/dispatch [::events/username-change %])
                   :type          "input"
                   :fullWidth     true
                   :tab-index     0
                   :onKeyUp       #(rf/dispatch [::events/login-on-key-enter %])}]))

(defn password-visibility-icon-button [show-password?]
  [RawIconButton {:el-id     [el :password-visibility]
                  :on-click  #(rf/dispatch [::events/toggle-password-visibility])
                  :tab-index 4}
   (if show-password?
     [VisibilityOffIcon]
     [VisibilityIcon])])

(defn password-field []
  (let [show-password? @(rf/subscribe [::subs/show-password?])
        loading?       @(rf/subscribe [::subs/loading?])]
    [RawTextField {:variant       "outlined"
                   :el-id         [el :password]
                   :key           "password"
                   :margin        "dense"
                   :default-value ""
                   :disabled      loading?
                   :label         (tr :password)
                   :on-change     #(rf/dispatch [::events/password-change %])
                   :type          (if show-password? "input" "password")
                   :suffix        (password-visibility-icon-button show-password?)
                   :fullWidth     true
                   :tab-index     1
                   :onKeyUp       #(rf/dispatch [::events/login-on-key-enter %])}]))

(defn confirm-login []
  (let [loading? @(rf/subscribe [::subs/loading?])]
    [RawTextField {:variant       "outlined"
                   :el-id         [el :confirmation-code]
                   :key           "confirm-login"
                   :margin        "dense"
                   :default-value ""
                   :disabled      loading?
                   :label         (tr :confirmation-code)
                   :on-change     #(rf/dispatch [::events/confirmation-code-change %])
                   :type          "input"
                   :fullWidth     true
                   :onKeyUp       #(rf/dispatch [::events/login-on-key-enter %])}]))

(defn password-requirement [label state]
  [RawGrid {:container true :spacing 1 :sx {:align-items "center" :width "100%"}}
   [RawGrid {}
    (case state
      :met     [TaskAltIcon {:color "success"}]
      :unmet   [ErrorOutlineIcon {:color "warning"}]
      :pending [TaskAltIcon {:color "disabled"}])]
   [RawGrid {:size "grow"}
    [RawTypography {:variant "subtitle2"}
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
    [RawGrid {:container true :spacing 2 :sx {:width "100%"}}
     [RawGrid {:size 12}
      (password-requirement (tr :password-requirements :upper-case) (state missing-upper-case?))]
     [RawGrid {:size 12}
      (password-requirement (tr :password-requirements :lower-case) (state missing-lower-case?))]
     [RawGrid {:size 12}
      (password-requirement (tr :password-requirements :number) (state missing-number?))]
     [RawGrid {:size 12}
      (password-requirement (tr :password-requirements :length) (state missing-length-8?))]]))

(defn login-dialog []
  (let [form-type @(rf/subscribe [::subs/form-type])
        show-error? @(rf/subscribe [::subs/error-message-visible])
        error-message @(rf/subscribe [::subs/error-message])]
    [RawDialog {:el-id           [el :dialog]
                :open            @(rf/subscribe [::subs/dialog-visible])
                :maxWidth        "xs"
                :fullWidth       true
                :onClose         #(rf/dispatch [::events/close-dialog])
                :aria-labelledby "form-dialog-title"
                :title-props     {:sx {:padding-right "48px"}}
                :title           [:<>
                                  (tr form-type)
                                  [dialog-close-button]]
                :actions         (login-dialog-actions form-type)}
     [RawGrid {:container true :spacing 1 :sx {:flex-direction "column" :width "100%"}}
      (when (some #(= % form-type) [:register :login :forgot-password])
        [RawGrid {:size "grow"}
         [Username]])

      (when (some #(= % form-type) [:register :login :confirm-password-reset])
        [RawGrid {} (password-field)])

      (when (some #(= % form-type) [:register :confirm-password-reset])
        [RawGrid {}
         [RawGrid {:container true :sx {:width "100%"}}
          [RawGrid {}
           (password-checks)]]])

      (when (some #(= % form-type) [:confirm-login :confirm-password-reset])
        [RawGrid {} (confirm-login)])

      (when show-error?
        [RawGrid {}
         [RawAlert {:severity "error"}
          (if (string? error-message)
            error-message
            (str (tr (or error-message :unknown-error))))]])

      (when (and show-error? (= :account-not-confirmed error-message))
        [RawGrid {:sx {:text-align "center"}}
         [RawLink {:sx       {:cursor "pointer"}
                   :on-click #(rf/dispatch [::events/confirm-account])}
          (tr :confirm-account)]])

      (when (= form-type :login)
        [RawGrid {:sx {:text-align "center"}}
         [RawTypography {:variant "body2"}
          [:<>
           (tr :no-account?) " "
           [RawLink {:sx       {:cursor "pointer"}
                     :on-click #(rf/dispatch [::events/open-dialog :register])}
            (tr :create-account)]]]])

      (when (= form-type :register)
        [RawGrid {:sx {:text-align "center"}}
         [RawTypography {:variant "body2"}
          [:<>
           (tr :have-account?) " "
           [RawLink {:sx       {:cursor "pointer"}
                     :on-click #(rf/dispatch [::events/open-dialog :login])}
            (tr :go-to-login)]]]])]]))

(defn logout-button []
  [RawGrid {}
   [RawButton {:el-id    [el :logout]
               :on-click #(rf/dispatch [::events/logout])}
    (tr :logout)]])

(defn register-button []
  [RawGrid {}
   [RawButton {:el-id    [el :register]
               :on-click #(rf/dispatch [::events/open-dialog :register])}
    (tr :register)]
   (login-dialog)])

(defn login-button []
  [RawGrid {}
   [RawButton {:el-id    [el :login]
               :on-click #(rf/dispatch [::events/open-dialog :login])}
    (tr :login)]])

(defn LoginBar [{:keys [enable-register login-provider]
                 :or   {enable-register true
                        login-provider  :username-password}
                 :as   params}]
  (let [logged? (boolean @(rf/subscribe [::subs/user-name]))
        init? @(rf/subscribe [::subs/init?])]
    (when init?
      [RawGrid {:container       true
                :spacing         3
                :sx {:justify-content :flex-end :width "100%"}}
       (when logged?
         (logout-button))
       (when (and enable-register
                  (false? logged?))
         (register-button))
       (when (false? logged?)
         (login-button))])))






