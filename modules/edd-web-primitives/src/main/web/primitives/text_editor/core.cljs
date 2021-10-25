(ns web.primitives.text-editor.core
  (:require
   ["suneditor-react" :default SunEditor]
   ["@mui/material/index" :refer [Grid Button IconButton]]
   [re-frame.core :as rf]
   [reagent.core :as r]

   [web.primitives.icons.core :refer [EditIcon]]
   [web.primitives.text-editor.utils :refer [sun-editor-button-list handle-on-paste handle-on-drop handle-on-save]]
   [web.primitives.text-editor.model :as model]))

(defn- read-only-mode [props set-edit-mode-funk]
  [:> Grid {:container true :style {:position "relative"}}
   [:> Grid {:container true
             :item true
             :style {:border "solid 1px #dadada"
                     :min-height "2rem"
                     :padding "1rem"
                     :display "block"
                     :font-size "13px"
                     :overflow "hidden"}
             :class-name "sun-editor-editable"
             :dangerouslySetInnerHTML {:__html (:set-contents props)}}]
   [:> Grid {:item true :style {:position "absolute" :right 0}}
    [:> IconButton
     (merge
      {:on-click set-edit-mode-funk
       :style    (merge
                  {:padding 0}
                  (when (true? (:disable props)) {:opacity 0.5}))
       :disabled (:disable props)}
      (when (:id props) {:id       (str "edit-button-" (:id props))}))
     [EditIcon]]]])

(defn- edit-mode [props set-read-only-mode]
  [:> Grid (merge
            {:container true}
            (when (:id props) {:id (str "editor-" (:id props))}))
   [:> SunEditor
    (merge
     {:on-paste (fn [event clean-data] (handle-on-paste event clean-data props))
      :on-drop (fn [] (handle-on-drop props))
      :setOptions  {:buttonList sun-editor-button-list
                    :resizingBar false
                    :showPathLabel false}
      :enableToolbar (not (:disable props))}
     props)]
   [:> Grid {:container true :style {:border "solid 1px #dadada"
                                     :border-top "none"
                                     :justify-content "flex-end"}}
    (when (contains? props :on-save)
      [:> Grid {:item true :style {:padding "1rem"}}
       [:> Button (merge
                   {:variant  "outlined"
                    :on-click (handle-on-save props set-read-only-mode)
                    :style    {:height "2rem"}}
                   (when (:id props) {:id (str "save-button-" (:id props))})) "Save"]
       [:> Button (merge
                   {:on-click set-read-only-mode
                    :style    {:height "2rem"}}
                   (when (:id props) {:id (str "discard-button-" (:id props))})) "Discard"]])]])

(defn RawTextEditor
  [{:keys [editor-mode set-read-only-mode set-edit-mode]
    :or {editor-mode :edit-mode}
    :as props}]
  (if
   (= :read-only-mode editor-mode)
    (read-only-mode props set-edit-mode)
    (edit-mode props set-read-only-mode)))

(defn EddTextEditor
  [props]
  (r/with-let [editor-id (keyword (str "rich-text-editor-mode-" (str (random-uuid))))]
    (let [editor-mode @(rf/subscribe [::model/get-editor-mode editor-id])
          set-read-only-mode (fn [] (rf/dispatch [::model/set-editor-mode editor-id :read-only-mode]))
          set-edit-mode (fn [] (rf/dispatch [::model/set-editor-mode editor-id :edit-mode]))]
      [RawTextEditor (merge
                      {:editor-mode editor-mode
                       :set-read-only-mode set-read-only-mode
                       :set-edit-mode set-edit-mode}
                      props)])))

