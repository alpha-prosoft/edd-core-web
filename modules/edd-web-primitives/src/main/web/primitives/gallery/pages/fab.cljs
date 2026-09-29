(ns web.primitives.gallery.pages.fab
  (:require
   [reagent.core :as r]
   [web.primitives.gallery.example :refer-macros [defdemo example]]
   [web.primitives.gallery.page :as page]
   [web.primitives.inputs.core :refer [RawFab]]
   [web.primitives.icons.core :refer [AddIcon]]))

(defdemo fab [variant args]
  (let [state (r/atom 0)]
    (fn []
      [:div
       (into [RawFab {:el-id (page/el :fab variant) :on-click #(swap! state inc)}]
             args)
       [:div {:data-testid (page/testid :fab variant :readout)}
        (str "clicks:" @state)]])))

(def ^:private props-doc
  [{:name :color    :type "string" :default "\"primary\"" :doc "Theme color."}
   {:name :size     :type "string" :default "\"large\""   :doc "small | medium | large."}
   {:name :on-click :type "fn"     :default "—"           :doc "Click handler."}
   {:name :el-id    :type "keyword | string" :default "—" :doc "Lands as data-testid."}])

(defn register! []
  (page/register!
   {:id    :fab
    :group "Inputs"
    :title "Floating action button"
    :doc   "A prominent round button for the primary action of a screen. Pass an icon, and optionally text for the extended variant. Animates in with a zoom."
    :props props-doc
    :variants
    [(example {:id :icon :title "Icon"}
              [fab :icon [[AddIcon {}]]])

     (example {:id :extended :title "Extended"}
              [fab :extended [[AddIcon {}] "Add"]])]}))
