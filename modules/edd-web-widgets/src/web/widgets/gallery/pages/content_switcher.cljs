(ns web.widgets.gallery.pages.content-switcher
  (:require
   [reagent.core :as r]
   [web.primitives.components :refer [RawButton RawGrid]]
   [web.primitives.gallery.example :refer-macros [defdemo example]]
   [web.primitives.gallery.page :as page]
   [web.widgets.content-switcher.views :refer [ContentSwitcher]]))

(defdemo delete-with-confirm []
  (let [checked? (r/atom false)]
    (fn []
      [ContentSwitcher {:checked? @checked?}
       [RawButton {:el-id    (page/el :content-switcher :basic)
                   :on-click #(reset! checked? true)}
        "Delete"]
       [RawGrid {:container true :spacing 2}
        [RawGrid {:size 12} "Are you sure?"]
        [RawGrid {}
         [RawButton {:el-id    (page/el :content-switcher :basic :yes)
                     :variant  "contained"
                     :on-click #(reset! checked? false)}
          "Yes"]]
        [RawGrid {}
         [RawButton {:variant  "outlined"
                     :on-click #(reset! checked? false)}
          "Cancel"]]]])))

(def ^:private props-doc
  [{:name :checked? :type "boolean" :default "false" :doc "Shows the checked content (third argument) instead of the default content (second argument)."}])

(defn register! []
  (page/register!
   {:id    :content-switcher
    :group "Widgets"
    :title "Content switcher"
    :doc   "Zooms between two pieces of content in place, e.g. a button and its inline confirmation."
    :props props-doc
    :variants
    [(example {:id :basic :title "Inline confirmation"}
              [delete-with-confirm])]}))
