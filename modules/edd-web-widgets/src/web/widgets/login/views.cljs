(ns web.widgets.login.views
  (:require [clojure.string :as str]
            [re-frame.core :as rf]
            [reagent.core :as r]
            [web.widgets.login.subs :as subs]
            [web.widgets.login.events :as events]
            [web.widgets.login.utils :as utils]
            [web.widgets.login.i18n.translate :refer [tr known?]]
            [web.primitives.testing.el-id :as el-id]
            [web.primitives.components :refer [RawAlert RawButton RawCircularProgress RawDialog
                                               RawGrid RawIconButton RawLink RawTextField
                                               RawTypography]]
            [web.primitives.icons.core :refer [CloseIcon ErrorOutlineIcon TaskAltIcon
                                               VisibilityIcon VisibilityOffIcon]]))

(def ^:private el :login-bar)

(defn field-id
  [field]
  (el-id/el-id [el field]))

(def form-id
  (field-id :form))

(def error-id
  (field-id :error))

(def ^:private title-id
  (field-id :dialog-title))

(def submit-elements
  {:login                  :submit-login
   :register               :submit-register
   :forgot-password        :submit-reset-password
   :confirm-password-reset :submit-password-reset
   :confirm-login          :submit-verification})

(def ^:private submit-labels
  {:login                  :login
   :register               :register
   :forgot-password        :reset-password
   :confirm-password-reset :confirm-reset-password
   :confirm-login          :confirm})

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

(defn- typed!
  "Stores a keystroke before React renders the field again, so fast typing or a password manager typing key by key
   loses no character."
  [event]
  (rf/dispatch-sync event)
  (r/flush))

(defn- field-values
  [^js form]
  (let [value-of
        (fn [field-name]
          (some-> (.namedItem (.-elements form) field-name) .-value))]
    {:username (value-of "email")
     :password (value-of "password")
     :code     (value-of "code")}))

(defn message-text
  [message]
  (cond
    (and (string? message) (not (str/blank? message))) message
    (and (keyword? message) (known? message))           (tr message)
    :else                                               (tr :unknown-error)))

(defn- error-aria
  [field]
  (let [form-type
        @(rf/subscribe [::subs/form-type])

        shown?
        @(rf/subscribe [::subs/error-message-visible])

        linked?
        (and shown?
             (= field (utils/error-field form-type @(rf/subscribe [::subs/error-message]))))]
    {:aria-describedby (when linked? error-id)
     :aria-invalid     linked?}))

(defn dialog-close-button
  ([]
   (dialog-close-button #(rf/dispatch [::events/close-dialog])))
  ([on-close]
   [RawIconButton {:el-id      [el :close]
                   :on-click   on-close
                   :aria-label (tr :close)
                   :sx         {:position "absolute"
                                :right    8
                                :top      8
                                :color    "grey.500"}}
    [CloseIcon]]))

(defn- busy-content
  []
  [RawCircularProgress {:size 20 :color "inherit" :aria-hidden true}])

(defn- submit-button
  [form-type loading?]
  [RawButton {:full-width true
              :variant    "outlined"
              :color      (if (= :login form-type) "primary" "secondary")
              :type       "submit"
              :form       form-id
              :el-id      [el (submit-elements form-type)]
              :aria-label (when loading? (tr :busy form-type))
              :disabled   loading?}
   (if loading?
     [busy-content]
     (tr (submit-labels form-type)))])

(defn- resend-button
  [loading?]
  (let [resending?
        @(rf/subscribe [::subs/resending?])]
    [RawButton {:full-width true
                :variant    "outlined"
                :el-id      [el :resend-code]
                :aria-label (when resending? (tr :busy :resend))
                :disabled   (or loading? resending?)
                :on-click   #(rf/dispatch [::events/resend-code])
                :color      "secondary"}
     (if resending?
       [busy-content]
       (tr :resend-code))]))

(defn login-dialog-actions [form-type]
  (let [loading?
        @(rf/subscribe [::subs/loading?])]
    [RawGrid {:container true
              :spacing   2
              :sx        {:padding "16px 16px 10px 16px" :margin "-24px 0 0 0" :width "100%"}}
     (when (= :login form-type)
       [RawGrid {:size 12
                 :sx   {:text-align "end"}}
        [RawLink {:sx        {:cursor "pointer"}
                  :el-id     [el :forgot-password]
                  :component "button"
                  :type      "button"
                  :on-click  #(rf/dispatch [::events/forgot-password])
                  :color     "primary"}
         (tr :forgot-password)]])

     [RawGrid {:size 12}
      [RawGrid {:container true
                :spacing   2
                :sx        {:width "100%"}}
       (when (utils/code-forms form-type)
         [RawGrid {:size {:xs 12 :sm 6}}
          [resend-button loading?]])
       (when (submit-elements form-type)
         [RawGrid {:size {:xs 12 :sm "grow"}}
          [submit-button form-type loading?]])]]]))

(defn Username []
  (let [form-type
        @(rf/subscribe [::subs/form-type])]
    [RawTextField {:variant      "outlined"
                   :el-id        [el :username]
                   :margin       "dense"
                   :name         "email"
                   :type         "email"
                   :autoComplete (if (= :register form-type) "email" "username")
                   :autoFocus    true
                   :value        (or @(rf/subscribe [::subs/username]) "")
                   :disabled     @(rf/subscribe [::subs/loading?])
                   :label        (tr :username)
                   :on-change    #(typed! [::events/username-change %])
                   :fullWidth    true
                   :slotProps    {:htmlInput (error-aria :username)}}]))

(defn password-visibility-icon-button [show-password?]
  [RawIconButton {:el-id        [el :password-visibility]
                  :aria-label   (tr (if show-password? :hide-password :show-password))
                  :aria-pressed (boolean show-password?)
                  :on-click     #(rf/dispatch [::events/toggle-password-visibility])}
   (if show-password?
     [VisibilityOffIcon]
     [VisibilityIcon])])

(defn password-field []
  (let [form-type
        @(rf/subscribe [::subs/form-type])

        show-password?
        @(rf/subscribe [::subs/show-password?])

        new-password?
        (contains? utils/new-password-forms form-type)]
    [RawTextField {:variant      "outlined"
                   :el-id        [el :password]
                   :margin       "dense"
                   :name         "password"
                   :autoComplete (if new-password? "new-password" "current-password")
                   :value        (or @(rf/subscribe [::subs/password]) "")
                   :disabled     @(rf/subscribe [::subs/loading?])
                   :label        (tr (if (= :confirm-password-reset form-type) :new-password :password))
                   :on-change    #(typed! [::events/password-change %])
                   :type         (if show-password? "text" "password")
                   :suffix       (password-visibility-icon-button show-password?)
                   :fullWidth    true
                   :slotProps    {:htmlInput (error-aria :password)}}]))

(defn confirm-login []
  [RawTextField {:variant      "outlined"
                 :el-id        [el :confirmation-code]
                 :margin       "dense"
                 :name         "code"
                 :autoComplete "one-time-code"
                 :autoFocus    true
                 :value        (or @(rf/subscribe [::subs/confirmation-code]) "")
                 :disabled     @(rf/subscribe [::subs/loading?])
                 :label        (tr :confirmation-code)
                 :on-change    #(typed! [::events/confirmation-code-change %])
                 :type         "text"
                 :fullWidth    true
                 :slotProps    {:htmlInput (merge {:inputMode "numeric"}
                                                  (error-aria :code))}}])

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

(defn- code-sent-to []
  [RawTypography {:variant "body2"
                  :el-id   [el :code-sent-to]}
   (str/replace (tr :code-sent-to) "{email}" (str (utils/sent-email @(rf/subscribe [::subs/username]))))])

(defn- note-link
  [{:keys [label] :as link}]
  [RawLink (merge {:sx {:cursor "pointer"}}
                  (dissoc link :label))
   label])

(defn register-note
  "The app's terms/privacy note under the sign-up form: a reagent component, a text, or `{:text :links}`."
  [note]
  (cond
    (fn? note)
    [note]

    (vector? note)
    note

    (string? note)
    [RawTypography {:variant "body2" :el-id [el :register-note]} note]

    (map? note)
    [RawTypography {:variant "body2" :el-id [el :register-note]}
     (into [:<> (:text note)]
           (mapcat (fn [index link]
                     [(if (zero? index) " " " · ")
                      [note-link link]])
                   (range)
                   (:links note)))]

    :else
    nil))

(defn- switch-link
  [question link-text form-type]
  [RawGrid {:sx {:text-align "center"}}
   [RawTypography {:variant "body2"}
    [:<>
     question " "
     [RawLink {:sx        {:cursor "pointer"}
               :el-id     [el :switch-to form-type]
               :component "button"
               :type      "button"
               :on-click  #(rf/dispatch [::events/open-dialog form-type])}
      link-text]]]])

(defn- dialog-form
  [form-type]
  (let [show-error?
        @(rf/subscribe [::subs/error-message-visible])

        error-message
        @(rf/subscribe [::subs/error-message])

        notice
        @(rf/subscribe [::subs/notice])

        note
        @(rf/subscribe [::subs/register-note])]
    [:form {:id          form-id
            :no-validate true
            :on-submit   (fn [event]
                           (.preventDefault event)
                           (rf/dispatch [::events/submit-form form-type (field-values (.-currentTarget event))]))}
     [RawGrid {:container true :spacing 1 :sx {:flex-direction "column" :width "100%"}}
      (when (and notice (utils/code-forms form-type))
        [RawGrid {}
         [RawAlert {:severity "info" :role "status" :el-id [el :notice]}
          (message-text notice)]])

      (when (= :confirm-login form-type)
        [RawGrid {} [code-sent-to]])

      (when (utils/email-forms form-type)
        [RawGrid {} [Username]])

      (when (utils/code-forms form-type)
        [RawGrid {} (confirm-login)])

      (when (#{:register :login :confirm-password-reset} form-type)
        [RawGrid {} (password-field)])

      (when (utils/new-password-forms form-type)
        [RawGrid {}
         [RawGrid {:container true :sx {:width "100%"}}
          [RawGrid {}
           (password-checks)]]])

      (when show-error?
        [RawGrid {}
         [RawAlert {:severity "error" :role "alert" :id error-id :el-id [el :error-message]}
          (message-text error-message)]])

      (when (and show-error? (= :account-not-confirmed error-message))
        [RawGrid {:sx {:text-align "center"}}
         [RawLink {:sx        {:cursor "pointer"}
                   :component "button"
                   :type      "button"
                   :on-click  #(rf/dispatch [::events/confirm-account])}
          (tr :confirm-account)]])

      (when (and note (= :register form-type))
        [RawGrid {} [register-note note]])

      (when (= form-type :login)
        [switch-link (tr :no-account?) (tr :create-account) :register])

      (when (= form-type :register)
        [switch-link (tr :have-account?) (tr :go-to-login) :login])]]))

(defonce ^:private phone?
  (let [media
        (when (exists? js/window.matchMedia)
          (.matchMedia js/window "(max-width: 599.95px)"))

        state
        (r/atom (boolean (some-> media .-matches)))]
    (when media
      (.addEventListener media "change" #(reset! state (.-matches %))))
    state))

(defn- focus-when-ready!
  [id]
  (letfn [(attempt [left]
            (let [element
                  (.getElementById js/document id)]
              (if (and element (not (.-disabled element)))
                (.focus element)
                (when (pos? left)
                  (js/setTimeout #(attempt (dec left)) 30)))))]
    (js/setTimeout #(attempt 20) 0)))

(defn- shown?
  [^js element]
  (and (some? element)
       (.-isConnected element)
       (not= element (.-body js/document))
       (if (fn? (.-checkVisibility element))
         (.checkVisibility element)
         (some? (.-offsetParent element)))))

(defn- fallback-element
  [fallback]
  (cond
    (string? fallback) (.querySelector js/document fallback)
    (fn? fallback)     (fallback)
    :else              nil))

(defonce ^:private last-focused
  (atom nil))

(defonce ^:private follow-focus!
  (delay
   (.addEventListener js/document
                      "focusin"
                      (fn [event]
                        (let [target
                              (.-target event)]
                          (when-not (.closest target "[role=dialog]")
                            (reset! last-focused target))))
                      true)))

(defn- login-dialog-view
  []
  @follow-focus!
  (let [opener
        (atom nil)

        was-open?
        (atom false)

        last-error
        (atom nil)

        closed-by-person?
        (atom false)

        fallback
        (atom nil)

        close!
        (fn []
          (reset! closed-by-person? true)
          (rf/dispatch [::events/close-dialog]))

        exited!
        (fn []
          (let [target
                (if (shown? @opener)
                  @opener
                  (fallback-element @fallback))]
            (when (and @closed-by-person? target)
              (.focus target)))
          (reset! closed-by-person? false))]
    (fn []
      (let [form-type
            @(rf/subscribe [::subs/form-type])

            open?
            @(rf/subscribe [::subs/dialog-visible])

            error
            (when @(rf/subscribe [::subs/error-message-visible])
              [form-type @(rf/subscribe [::subs/error-message]) @(rf/subscribe [::subs/submits])])]
        (when (and open? (not @was-open?))
          (let [active
                (.-activeElement js/document)]
            (reset! opener (if (shown? active) active @last-focused))))
        (reset! was-open? open?)
        (reset! fallback @(rf/subscribe [::subs/focus-fallback]))
        (when (and open? error (not= error @last-error))
          (focus-when-ready! (field-id (utils/error-field form-type (second error)))))
        (reset! last-error error)
        [RawDialog {:el-id               [el :dialog]
                    :open                open?
                    :maxWidth            "xs"
                    :fullWidth           true
                    :fullScreen          @phone?
                    :disableRestoreFocus true
                    :TransitionProps     {:onExited exited!}
                    :onClose             close!
                    :aria-labelledby     title-id
                    :title-props         {:id (field-id :dialog-heading)
                                          :sx {:padding-right "48px"}}
                    :title               [:<>
                                          [:span {:id title-id} (tr :titles form-type)]
                                          [dialog-close-button close!]]
                    :actions             [login-dialog-actions form-type]}
         [dialog-form form-type]]))))

(defn login-dialog []
  [login-dialog-view])

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
