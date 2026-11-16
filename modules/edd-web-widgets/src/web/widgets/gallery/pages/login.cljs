(ns web.widgets.gallery.pages.login
  (:require
   [re-frame.core :as rf]
   [reagent.core :as r]
   [web.primitives.components :refer [RawButton RawGrid]]
   [web.primitives.gallery.example :refer-macros [defdemo example]]
   [web.primitives.gallery.page :as page]
   [web.widgets.login.core]
   [web.widgets.login.db :as db]
   [web.widgets.login.events :as events]
   [web.widgets.login.views :refer [LoginBar]]))

(def ^:private states
  [[:login "Log in" {::db/form-type :login}]
   [:remembered "Log in: email remembered" {::db/form-type :login
                                            ::db/username  "someone@example.com"}]
   [:refused "Login refused" {::db/form-type             :login
                              ::db/username              "someone@example.com"
                              ::db/password              "Wrong-password-1"
                              ::db/error-message-visible true
                              ::db/error-message         :invalid-credentials}]
   [:register "Create an account" {::db/form-type :register}]
   [:forgot "Reset: address" {::db/form-type :forgot-password}]
   [:forgot-busy "Reset: sending" {::db/form-type :forgot-password
                                   ::db/username  "someone@example.com"
                                   ::db/loading?  true}]
   [:forgot-offline "Reset: offline" {::db/form-type             :forgot-password
                                      ::db/username              "someone@example.com"
                                      ::db/error-message-visible true
                                      ::db/error-message         :network-error}]
   [:reset-code "Reset: code sent" {::db/form-type :confirm-password-reset
                                    ::db/username  "someone@example.com"
                                    ::db/notice    :reset-code-sent}]
   [:code-resent "Reset: code resent" {::db/form-type :confirm-password-reset
                                       ::db/username  "someone@example.com"
                                       ::db/notice    :code-resent}]
   [:confirm "Confirm sign-up" {::db/form-type :confirm-login
                                ::db/username  "someone@example.com"}]])

(rf/reg-event-db
 ::show
 (fn [db [_ state]]
   (merge db
          {::db/username              ""
           ::db/password              ""
           ::db/confirmation-code     ""
           ::db/error-message-visible false
           ::db/error-message         ""
           ::db/notice                nil
           ::db/loading?              false
           ::db/resending?            false
           ::db/dialog-visible        true}
          state)))

(def ^:private options
  {:register-note  {:text  "By creating an account you accept the"
                    :links [{:label "terms" :href "#terms"}
                            {:label "privacy policy" :href "#privacy"}]}
   :focus-fallback "[data-testid='login-bar--register']"
   :retry-delays   [1000 3000]})

(defonce ^:private announced
  (r/atom ""))

(defn- announce!
  [text]
  (reset! announced text))

(defdemo login-bar [props]
  (rf/dispatch-sync [::events/initialize-db])
  (rf/dispatch-sync [::events/init options])
  (fn [props]
    [:div {:data-testid (page/testid :login :bar)}
     [LoginBar props]]))

(defdemo login-states []
  [RawGrid {:container true :spacing 1}
   (for [[id label state] states]
     ^{:key id}
     [RawGrid {}
      [RawButton {:el-id    (page/el :login :states id)
                  :variant  "outlined"
                  :on-click #(rf/dispatch [::show state])}
       label]])])

(defdemo login-announcer []
  [RawGrid {:container true :spacing 1 :sx {:align-items "center"}}
   [RawGrid {}
    [RawButton {:el-id    (page/el :login :announcer :app)
                :variant  "outlined"
                :on-click #(rf/dispatch [::events/init (assoc options :announce announce!)])}
     "Notices through the app (:announce)"]]
   [RawGrid {}
    [RawButton {:el-id    (page/el :login :announcer :dialog)
                :variant  "outlined"
                :on-click #(rf/dispatch [::events/init options])}
     "Notices in the dialog's live region"]]
   [RawGrid {}
    [:div {:data-testid (page/testid :login :announced)
           :aria-live   "polite"}
     @announced]]])

(def ^:private props-doc
  [{:name :enable-register :type "boolean" :default "true" :doc "Show the register button next to login."}
   {:name :login-provider  :type "keyword" :default ":username-password" :doc "How users sign in."}])

(defn register! []
  (page/register!
   {:id    :login
    :group "Widgets"
    :title "Login bar"
    :doc   "Login, register and logout buttons with the login dialog. Shown once ::initialize-db ran; signing in needs the Cognito config from login.core/init. Options go to ::events/init: :remember-email (default true), :register-note, :translations, :focus-fallback, :announce (the app's announcer instead of the dialog's live region), :retry-delays (default [1000 3000], [] for none), :region (default: from the user pool id)."
    :props props-doc
    :variants
    [(example {:id :bar :title "Logged out"}
              [login-bar {}])
     (example {:id :states :title "Dialog states (opens the dialog of the bar above)"}
              [login-states])
     (example {:id :announcer :title "Who says the notices (the reset states above show one)"}
              [login-announcer])]}))
