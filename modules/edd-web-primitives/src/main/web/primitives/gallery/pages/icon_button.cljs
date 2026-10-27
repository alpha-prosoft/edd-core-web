(ns web.primitives.gallery.pages.icon-button
  (:require
   [reagent.core :as r]
   [web.primitives.gallery.example :refer-macros [defdemo example]]
   [web.primitives.gallery.page :as page]
   [web.primitives.inputs.core :refer [RawIconButton]]
   [web.primitives.icons.core :refer [EditIcon]]))

(defdemo clickable [variant props]
  (let [state (r/atom 0)]
    (fn []
      [:div
       [RawIconButton
        (merge {:el-id    (page/el :icon-button variant)
                :on-click #(swap! state inc)}
               props)
        [EditIcon {}]]
       [:div {:data-testid (page/testid :icon-button variant :readout)}
        (str "clicks:" @state)]])))

(defdemo link []
  [RawIconButton {:el-id  (page/el :icon-button :link)
                  :href   "#icon-button"
                  :sx     {:color "text.secondary"}}
   [EditIcon {:font-size "small"}]])

(def ^:private props-doc
  [{:name :size     :type "\"small\" | \"medium\"" :default "\"medium\"" :doc "Icon size (20px / 24px)."}
   {:name :on-click :type "fn"      :default "—"     :doc "Click handler."}
   {:name :href     :type "string"  :default "nil"   :doc "Renders a link."}
   {:name :disabled :type "boolean" :default "false" :doc "Disabled automatically when no icon is given."}
   {:name :el-id    :type "keyword | string" :default "—" :doc "Lands as data-testid."}])

(defn register! []
  (page/register!
   {:id    :icon-button
    :group "Inputs"
    :title "Icon button"
    :doc   "A button with just an icon. The icon is sized by `:size` unless it brings its own props. `EddIconButton` can also swap to a hover icon."
    :props props-doc
    :variants
    [(example {:id :medium :title "Medium"}
              [clickable :medium {}])

     (example {:id :small :title "Small"}
              [clickable :small {:size "small"}])

     (example {:id :link :title "Link with own icon props"}
              [link])]}))
