(ns web.primitives.text-editor.utils
  (:require [clojure.string :as str]))

(defn deep-merge
  [& maps]
  (apply merge-with (fn [left right]
                      (if (and (map? left) (map? right))
                        (deep-merge left right)
                        right))
         maps))

(def sun-editor-font-size-list
  [8 10 13 14 15 16 18 20 22 26 28 36 48 72])

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

(def sun-editor-font-list
  ["Arial" "Comic Sans MS" "Courier New" "Impact" "Georgia" "Tahoma" "Trebuchet MS" "Verdana"])

(defn- append-style-declaration
  [style declaration]
  (let [style (or style "")]
    (str style
         (when (and (not (str/blank? style))
                    (not (str/ends-with? style ";")))
           ";")
         declaration)))

(defn normalize-sun-editor-options
  [options]
  (let [{:keys [font fontFamily editorStyle] :as options} (or options {})
        legacy-font-options (when (map? fontFamily) (:options fontFamily))
        legacy-default-font (when (map? fontFamily) (:default fontFamily))
        has-font-family-style? (boolean (re-find #"(?i)(^|;)\s*font-family\s*:" (or editorStyle "")))]
    (cond-> options
      true
      (dissoc :fontFamily)

      (and (seq legacy-font-options) (map? font))
      (update :font merge {:items legacy-font-options})

      (and (seq legacy-font-options) (nil? font))
      (assoc :font {:items legacy-font-options})

      (and (seq legacy-default-font) (not has-font-family-style?))
      (assoc :editorStyle (append-style-declaration editorStyle (str "font-family: " legacy-default-font ";"))))))

(def sun-editor-default-options
  {:buttonList sun-editor-button-list
   :statusbar  false
   :editableFrameAttributes {:spellcheck "true"}
   :font {:items sun-editor-font-list}
   :image {:defaultWidth "auto"
           :defaultHeight "auto"}
   :fontSize   {:unitMap {:px {:default-size 14
                               :inc     1
                               :min     8
                               :max     72
                               :list    sun-editor-font-size-list}}}})

(defn handle-on-paste
  [_ _ props]
  (not (:disable props)))

(defn handle-on-drop
  [props]
  (not (:disable props)))

(defn handle-on-save
  [props set-read-only-mode]
  (comp (:on-save props) set-read-only-mode))
