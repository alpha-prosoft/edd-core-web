(ns web.primitives.sdk.text-editor
  "EddTextEditor / RawTextEditor. The editor uses el-id as its id and derives
   the editable area and the edit / save / discard buttons from it."
  (:require
   [clojure.string :as str]
   [web.primitives.sdk.core :as sdk]))

(defn- id [el-id]
  (sdk/el-id->string el-id))

(defn- editable
  "The SunEditor contenteditable area; the primitive gives it the id
   \"text-editor-editable-<el-id>\"."
  [el-id]
  (sdk/el (str "text-editor-editable-" (id el-id))))

(defn- read-view [el-id]
  (.locator (sdk/el (str "edit-button-" (id el-id)))
            "xpath=ancestor::div[contains(@class,'MuiGrid-container')][1]//div[contains(@class,'se-container')]"))

(defn- edit-button [el-id]
  (sdk/el (str "edit-button-" (id el-id))))

(defn wait-ready!
  "SunEditor mounts asynchronously. Wait until the editor is shown in
   either edit mode (editable area) or read mode (edit button)."
  [el-id]
  (sdk/eventually #(or (pos? (.count (editable el-id)))
                       (pos? (.count (edit-button el-id)))))
  el-id)

(defn editing?
  "True when the rich text editor is in edit mode."
  [el-id]
  (wait-ready! el-id)
  (pos? (.count (editable el-id))))

(defn type!
  "Append text at the end of the editor content."
  [el-id text]
  (let [area
        (sdk/wait-visible (editable el-id))]
    (.click area)
    (sdk/press! "End")
    (.pressSequentially area (str text))))

(defn text
  "Plain text of the editor, in edit or read mode."
  [el-id]
  (str/trim
   (if (editing? el-id)
     (.innerText (editable el-id))
     (.innerText (read-view el-id)))))

(defn start-editing!
  "Press the edit (pencil) button shown in read mode."
  [el-id]
  (wait-ready! el-id)
  (.click (edit-button el-id))
  (sdk/wait-visible (editable el-id)))

(defn save!
  "Press Save and wait for the editor to return to read mode."
  [el-id]
  (sdk/click! (str "save-button-" (id el-id)))
  (sdk/wait-visible (edit-button el-id)))

(defn discard!
  "Press Discard and wait for the editor to return to read mode."
  [el-id]
  (sdk/click! (str "discard-button-" (id el-id)))
  (sdk/wait-visible (edit-button el-id)))
