(ns web.primitives.gallery.pages.text-editor
  (:require
   [reagent.core :as r]
   [web.primitives.gallery.example :refer-macros [defdemo example]]
   [web.primitives.gallery.page :as page]
   [web.primitives.text-editor.core :refer [EddTextEditor]]))

(defdemo editor [variant props]
  (let [state (r/atom {:draft nil :value (:set-contents props)})]
    (fn []
      [:div
       [EddTextEditor
        (merge {:el-id        (page/el :text-editor variant)
                :placeholder  "Type here..."
                :editor-mode  :edit-mode
                :set-contents (:value @state)
                :on-change    (fn [x] (swap! state assoc :draft x))}
               props)]])))

(defdemo editable-with-save [variant initial-html]
  (let [state (r/atom {:draft initial-html :saved initial-html})]
    (fn []
      [:div
       [EddTextEditor
        {:el-id        (page/el :text-editor variant)
         :disable      false
         :set-contents (:saved @state)
         :on-change    (fn [html] (swap! state assoc :draft html))
         :on-save      (fn [] (swap! state assoc :saved (:draft @state)))}]
       [:div {:data-testid (page/testid :text-editor variant :readout)}
        (str "saved:" (:saved @state))]])))

(def ^:private props-doc
  [{:name :set-contents       :type "string (html)" :default "nil" :doc "HTML content to load. Sanitised before display."}
   {:name :on-change          :type "fn"      :default "—"     :doc "Called with the editor HTML on change."}
   {:name :on-save            :type "fn"      :default "nil"   :doc "When set, shows Save/Discard buttons."}
   {:name :editor-mode        :type ":edit-mode | :read-only-mode" :default ":edit-mode" :doc "Initial mode."}
   {:name :placeholder        :type "string"  :default "nil"   :doc "Placeholder text."}
   {:name :disable            :type "boolean" :default "false" :doc "Force read only."}
   {:name :edit-icon-position :type ":left | :right" :default ":right" :doc "Where the edit button sits in read mode."}
   {:name :el-id              :type "keyword | string" :default "—" :doc "Used as the editor id; buttons derive their ids from it."}])

(defn register! []
  (page/register!
   {:id    :text-editor
    :group "Inputs"
    :title "Text editor"
    :doc   "Rich text editor (SunEditor). Content is HTML and is sanitised before display."
    :props props-doc
    :variants
    [(example {:id :edit :title "Edit mode"}
              [editor :edit {}])

     (example {:id :read-only :title "Read only"}
              [editor :read-only {:editor-mode  :read-only-mode
                                  :set-contents "<p>Read only</p>"}])

     (example {:id :with-save :title "Edit, save and discard"}
              [editable-with-save :with-save "<p>Saved text</p>"])]}))
