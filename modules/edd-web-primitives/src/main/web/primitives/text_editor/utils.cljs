(ns web.primitives.text-editor.utils)

(def sun-editor-button-list
  [["undo" "redo"]
   ["font" "fontSize" "blockStyle"]
   ["blockquote"]
   ["bold" "underline" "italic" "strike" "subscript" "superscript"]
   ["removeFormat"]
   ["fontColor" "backgroundColor"]
   ["outdent" "indent"]
   ["align" "hr" "list" "table"]
   ["link" "image"]
   ["fullScreen"]
   ["preview" "print"]])

(defn handle-on-paste
  [_ _ props]
  (not (:disable props)))

(defn handle-on-drop
  [props]
  (not (:disable props)))

(defn handle-on-save
  [props set-read-only-mode]
  (comp (:on-save props) set-read-only-mode))

