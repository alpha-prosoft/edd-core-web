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
  [8 10 12 13 14 15 16 18 20 22 26 28 36 48 72])

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
  ["Amalia" "Arial" "Comic Sans MS" "Courier New" "Impact" "Georgia" "Tahoma" "Trebuchet MS" "Verdana"])

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
  (let [{:keys [font fontFamily editorStyle defaultStyle] :as options} (or options {})
        legacy-font-options (when (map? fontFamily) (:options fontFamily))
        legacy-default-font (when (map? fontFamily) (:default fontFamily))
        base-style (or defaultStyle editorStyle "")
        has-font-family-style? (boolean (re-find #"(?i)(^|;)\s*font-family\s*:" base-style))]
    (cond-> options
      true
      (dissoc :fontFamily)

      (and (seq legacy-font-options) (map? font))
      (update :font merge {:items legacy-font-options})

      (and (seq legacy-font-options) (nil? font))
      (assoc :font {:items legacy-font-options})

      (and (seq legacy-default-font) (not has-font-family-style?))
      (assoc :defaultStyle (append-style-declaration base-style (str "font-family: " legacy-default-font ";"))))))

(defn sync-li-style-marker [html]
  (let [container (.createElement js/document "div")]
    (set! (.-innerHTML container) (or html ""))
    ;; Normalize list-item formatting to span-only styles.
    (doseq [li (array-seq (.querySelectorAll container "li"))]
      (let [style (some-> (.getAttribute li "style") str/trim)
            marker (some-> (.getAttribute li "data-se-li-style") str/trim)]
        (when (or (seq style) (seq marker))
          (let [li-style (or style marker)
                first-span (.querySelector li "span")
                carrier (or first-span
                            (let [span (.createElement js/document "span")
                                  fragment (.createDocumentFragment js/document)]
                              (loop [child (.-firstChild li)]
                                (when child
                                  (let [next (.-nextSibling child)]
                                    (.appendChild fragment child)
                                    (recur next))))
                              (.appendChild span fragment)
                              (.appendChild li span)
                              span))
                span-style (some-> (.getAttribute carrier "style") str/trim)
                merged-style (if (str/blank? span-style)
                               li-style
                               (str li-style ";" span-style))]
            (.setAttribute carrier "style" merged-style)))
        (.removeAttribute li "style")
        (.removeAttribute li "data-se-li-style")))
    (.-innerHTML container)))

(def sun-editor-default-options
  {:buttonList sun-editor-button-list
   :statusbar  false
   :editableFrameAttributes {:spellcheck "true"}
   :editorStyle "--se-content-font-family: Amalia; --se-edit-font-size: 14px; font-family: Amalia; font-size: 14px;"
   :defaultStyle "font-family: Amalia; font-size: 14px;"
   :font {:items sun-editor-font-list}
   :image {:defaultWidth "auto"
           :defaultHeight "auto"}
   :fontSize   {:unitMap {:px {:inc     1
                               :min     8
                               :max     72
                               :list    sun-editor-font-size-list}}}})

(defn strip-invisible-separators [value]
  (-> (or value "")
      (str/replace #"[\u200B\u200C\u200D\u200E\u200F\u202A\u202B\u202C\u202D\u202E\u2060\u2066\u2067\u2068\u2069\uFEFF]|&ZeroWidthSpace;|&#8203;|&#x200B;|&zwnj;|&zwj;" "")))

(def single-cell-copy-marker "<!--edd-single-cell-copy-->")

(defn- blank-text-node? [node]
  (and (= (.-nodeType node) js/Node.TEXT_NODE)
       (str/blank? (strip-invisible-separators (or (.-nodeValue node) "")))))

(defn- unwrap-single-block-wrapper [html]
  (let [container (.createElement js/document "div")]
    (set! (.-innerHTML container) (or html ""))
    (let [child (.-firstElementChild container)
          tag (some-> child .-tagName str/lower-case)
          nodes (array-seq (.-childNodes container))
          only-wrapper? (and child
                             (#{"div" "p"} tag)
                             (not-any? (fn [node]
                                         (and (not= node child)
                                              (not (blank-text-node? node))))
                                       nodes))]
      (if only-wrapper?
        (.-innerHTML child)
        (or html "")))))

(defn normalize-copied-cell-html [html]
  (-> (strip-invisible-separators html)
      (str/replace #"(?is)\A(?:\s*<(?:p|div)>\s*(?:<br\s*/?>)?\s*</(?:p|div)>)+" "")
      (unwrap-single-block-wrapper)
      (str/replace #"(?is)\A(?:\s*<br\s*/?>)+" "")))

(defn plain-text-from-html [html]
  (let [container (.createElement js/document "div")]
    (set! (.-innerHTML container) (or html ""))
    (-> (or (.-textContent container) "")
        (strip-invisible-separators)
        (str/replace #"\A(?:\s*\n)+" "")
        (str/replace #"(?:\n\s*)+\z" ""))))

(defn copied-cell-payload [single-cell]
  (let [html (normalize-copied-cell-html (.-innerHTML single-cell))
        text (plain-text-from-html html)]
    {:text text
     :html (str single-cell-copy-marker html)}))

(defn normalize-leading-paste-html [html]
  (-> (strip-invisible-separators html)
      (str/replace single-cell-copy-marker "")
      (str/replace #"(?is)\A(?:\s*<(?:p|div)>\s*(?:<br\s*/?>)?\s*</(?:p|div)>)" "")
      (str/replace #"(?is)\A(?:\s*<br\s*/?>)+" "")))

(defn normalize-leading-paste-text [text]
  (-> (strip-invisible-separators text)
      (str/replace #"\A(?:\s*\n)+" "")))

(defn resolve-selected-single-cell [editable selection]
  (let [selected-cells (.querySelectorAll editable ".se-selected-table-cell")
        selected-count (.-length selected-cells)
        focused-cell (.querySelector editable ".se-selected-cell-focus")]
    (cond
      (> selected-count 1)
      nil

      (= selected-count 1)
      (.item selected-cells 0)

      focused-cell
      focused-cell

      :else
      (let [node (when selection (.-anchorNode selection))
            element (cond
                      (nil? node) nil
                      (= (.-nodeType node) js/Node.TEXT_NODE) (.-parentElement node)
                      :else node)
            cell (when element (.closest element "td,th"))]
        (when (and cell (.contains editable cell))
          cell)))))

(defn- empty-block-element? [el]
  (let [tag (some-> el .-tagName str/lower-case)
        html (-> (or (.-innerHTML el) "") strip-invisible-separators str/trim)]
    (and (#{"div" "p"} tag)
         (or (= "" html)
             (boolean (re-matches #"(?is)(?:<br\s*/?>|&nbsp;|\s)*" html))))))

(defn cleanup-leading-paste-empty-line! []
  (let [selection (.getSelection js/window)
        node (when selection (.-anchorNode selection))
        element (cond
                  (nil? node) nil
                  (= (.-nodeType node) js/Node.TEXT_NODE) (.-parentElement node)
                  :else node)
        block (when element (.closest element "div,p,td,th"))]
    (when block
      (when-let [prev (.-previousElementSibling block)]
        (when (empty-block-element? prev)
          (.remove prev)))
      (loop []
        (let [first-child (.-firstChild block)]
          (when first-child
            (cond
              (and (= (.-nodeType first-child) js/Node.TEXT_NODE)
                   (str/blank? (strip-invisible-separators (or (.-nodeValue first-child) ""))))
              (do (.remove first-child)
                  (recur))

              (and (= (.-nodeType first-child) js/Node.ELEMENT_NODE)
                   (empty-block-element? first-child))
              (do (.remove first-child)
                  (recur))

              (and (= (.-nodeType first-child) js/Node.ELEMENT_NODE)
                   (= "br" (some-> first-child .-tagName str/lower-case)))
              (do (.remove first-child)
                  (recur)))))))))

(defn- selected-table-cell-target? [cell]
  (and cell
       (or (.contains (.-classList cell) "se-selected-table-cell")
           (.contains (.-classList cell) "se-selected-cell-focus"))))

(defn- stop-event! [event]
  (.preventDefault event)
  (when-let [stop-immediate (.-stopImmediatePropagation event)]
    (.call stop-immediate event))
  (when-let [stop-propagation (.-stopPropagation event)]
    (.call stop-propagation event)))

(defn- resolve-paste-target-cell [editable event]
  (let [selection (.getSelection js/window)
        target (.-target event)
        event-cell (when target (.closest target "td,th"))
        selected-cell (when editable (resolve-selected-single-cell editable selection))]
    (cond
      (and editable event-cell (.contains editable event-cell)) event-cell
      (and editable selected-cell (.contains editable selected-cell)) selected-cell
      :else nil)))

(defn- clipboard-has-multi-cell-selection? [html text]
  (let [text (or text "")
        tabular-text? (str/includes? text "\t")
        html (or html "")
        tabular-html?
        (when (seq html)
          (let [container (.createElement js/document "div")]
            (set! (.-innerHTML container) html)
            (let [table (.querySelector container "table")
                  cells (when table (.querySelectorAll table "td,th"))]
              (and cells (> (.-length cells) 1)))))]
    (or tabular-text? tabular-html?)))

(defn- single-block-wrapper-tag [cell]
  (let [child (.-firstElementChild cell)
        tag (some-> child .-tagName str/lower-case)
        nodes (array-seq (.-childNodes cell))]
    (when (and child
               (#{"div" "p"} tag)
               (not-any? (fn [node]
                           (and (not= node child)
                                (not (blank-text-node? node))))
                         nodes))
      tag)))

(defn- plain-text->html [text]
  (let [container (.createElement js/document "div")]
    (set! (.-textContent container) (or text ""))
    (.-innerHTML container)))

(defn- normalize-table-cell-replacement-html [target-cell html text]
  (let [content (if (and (string? html) (not (str/blank? html)))
                  html
                  (plain-text->html text))
        content (if (str/blank? content) "<br>" content)
        wrapper-tag (single-block-wrapper-tag target-cell)
        already-block? (boolean (re-find #"(?is)\A\s*<(?:div|p)\b" content))]
    (cond
      wrapper-tag
      (str "<" wrapper-tag ">" content "</" wrapper-tag ">")

      already-block?
      content

      :else
      (str "<div>" content "</div>"))))

(defn- place-caret-at-end! [cell]
  (let [selection (.getSelection js/window)
        range (.createRange js/document)
        caret-root (or (.-lastChild cell) cell)]
    (.selectNodeContents range caret-root)
    (.collapse range false)
    (.removeAllRanges selection)
    (.addRange selection range)))

(defn- replace-selected-table-cell-paste! [editable event html text marker-paste?]
  (let [selection (.getSelection js/window)
        has-range? (and selection (pos? (.-rangeCount selection)))
        has-text-selection? (and has-range? (not (.-isCollapsed selection)))
        target-cell (resolve-paste-target-cell editable event)
        has-html? (and (string? html) (not (str/blank? html)))]
    (when (and target-cell
               (or marker-paste?
                   (selected-table-cell-target? target-cell))
               (not has-text-selection?))
      (stop-event! event)
      (set! (.-innerHTML target-cell)
            (normalize-table-cell-replacement-html target-cell (when has-html? html) text))
      (place-caret-at-end! target-cell)
      (.dispatchEvent editable (js/Event. "input" #js {:bubbles true}))
      (js/setTimeout cleanup-leading-paste-empty-line! 0)
      true)))

(defn maybe-normalize-paste-artifacts! [event editable]
  (let [clipboard-data (.-clipboardData event)
        html-raw (when clipboard-data (.getData clipboard-data "text/html"))
        text-raw (when clipboard-data (.getData clipboard-data "text/plain"))
        html (or html-raw "")
        text (or text-raw "")
        marker-paste? (str/includes? html single-cell-copy-marker)
        multi-cell-clipboard? (clipboard-has-multi-cell-selection? html-raw text-raw)
        html-normalized (normalize-leading-paste-html html)
        text-normalized (normalize-leading-paste-text text)
        selected-cell-paste? (and (not multi-cell-clipboard?)
                                  (boolean (resolve-paste-target-cell editable event)))
        should-normalize? (or marker-paste?
                              selected-cell-paste?
                              (not= html html-normalized)
                              (not= text text-normalized))]
    (when (and should-normalize? (not multi-cell-clipboard?))
      (when-not (replace-selected-table-cell-paste! editable event html-normalized text-normalized marker-paste?)
        (stop-event! event)
        (if (and (seq html-normalized) (not (str/blank? html-normalized)))
          (js/document.execCommand "insertHTML" false html-normalized)
          (js/document.execCommand "insertText" false text-normalized))
        (js/setTimeout cleanup-leading-paste-empty-line! 0)))))

(defn handle-on-paste
  [_ _ props]
  (not (:disable props)))

(defn handle-on-drop
  [props]
  (not (:disable props)))

(defn handle-on-save
  [props set-read-only-mode]
  (fn [event]
    (let [on-save (:on-save props)]
      (if on-save
        (let [result (on-save event)
              then-fn (when result (.-then result))]
          (if (fn? then-fn)
            (.then result
                   (fn [_] (set-read-only-mode))
                   (fn [_] nil))
            (set-read-only-mode)))
        (set-read-only-mode)))))
