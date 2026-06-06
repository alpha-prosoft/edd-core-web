(ns edd.db
  (:require
   [re-frame.db :as re-frame-db]
   [reagent.ratom :as ratom]))

(defn non-reactive-db
  "Read app-db without subscribing to it. Dereferencing app-db inside a Reagent
   reactive context couples that computation to every db change; this read is
   safe to call from anywhere, including render."
  []
  (binding [ratom/*ratom-context* nil]
    @re-frame-db/app-db))

(def default-db
  {::user                     nil
   ::active-panel             :home
   ::drawer                   false
   ::ready                    true
   ::selected-language        :en
   ::show-language-switcher?  true
   ::menu-items               {}
   ::menu-expanded            {}
   ::languages                {}
   ::translations             {}
   ::config                   {}
   ::routes                   {}
   ::pages-init-events        {}
   ::url-params               {}
   ::path-params              {}
   ::pages-url-params         {}
   ::error-pages              {}
   ::error                    nil
   ::record-call-failure-func nil
   ::record-call-func         nil
   ::on-expired-jwt-func      nil})
