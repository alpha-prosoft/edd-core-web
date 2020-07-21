(ns web.primitives.text-editor.model
  (:require
   [web.primitives.db :as db]
   [re-frame.core :as rf]))

(rf/reg-event-db
 ::set-editor-mode
 (fn [db [_ editor-id mode]]
   (assoc-in db [::db/rich-text-editor-mode editor-id] mode)))

(rf/reg-sub
 ::get-editor-mode
 (fn [{:keys [::db/rich-text-editor-mode]} [_ editor-id]]
   (get-in rich-text-editor-mode [editor-id] :read-only-mode)))
