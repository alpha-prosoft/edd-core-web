(ns web.widgets.gallery.pages.login
  (:require
   [re-frame.core :as rf]
   [web.primitives.gallery.example :refer-macros [defdemo example]]
   [web.primitives.gallery.page :as page]
   [web.widgets.login.events :as events]
   [web.widgets.login.views :refer [LoginBar]]))

(defdemo login-bar [props]
  (rf/dispatch-sync [::events/initialize-db])
  (fn [props]
    [:div {:data-testid (page/testid :login :bar)}
     [LoginBar props]]))

(def ^:private props-doc
  [{:name :enable-register :type "boolean" :default "true" :doc "Show the register button next to login."}
   {:name :login-provider  :type "keyword" :default ":username-password" :doc "How users sign in."}])

(defn register! []
  (page/register!
   {:id    :login
    :group "Widgets"
    :title "Login bar"
    :doc   "Login, register and logout buttons with the login dialog. Shown once ::initialize-db ran; signing in needs the Cognito config from login.core/init."
    :props props-doc
    :variants
    [(example {:id :bar :title "Logged out"}
              [login-bar {}])]}))
