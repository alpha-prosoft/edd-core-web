(ns web.primitives.text-editor.core-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [clojure.string :as str]
            [web.primitives.text-editor.utils :as sut]))

(defn- set-collapsed-selection! [node offset]
  (let [selection (.getSelection js/window)
        range (.createRange js/document)]
    (.setStart range node offset)
    (.collapse range true)
    (.removeAllRanges selection)
    (.addRange selection range)))

(defn- with-stubbed-exec-command [f]
  (let [original (.-execCommand js/document)]
    (set! (.-execCommand js/document)
          (fn [command _show-ui value]
            (let [selection (.getSelection js/window)
                  has-range? (and selection (pos? (.-rangeCount selection)))
                  range (when has-range? (.getRangeAt selection 0))]
              (when range
                (.deleteContents range)
                (if (= command "insertHTML")
                  (.insertNode range (.createContextualFragment range value))
                  (.insertNode range (.createTextNode js/document (or value ""))))))
            true))
    (try
      (f)
      (finally
        (set! (.-execCommand js/document) original)
        (.removeAllRanges (.getSelection js/window))))))

(defn- clipboard-event
  ([target html text prevented?]
   (clipboard-event target html text prevented? nil nil))
  ([target html text prevented? stopped? immediate-stopped?]
   (let [event (js-obj)]
     (aset event "target" target)
     (aset event "clipboardData"
           #js {:getData (fn [kind]
                           (case kind
                             "text/html" html
                             "text/plain" text
                             ""))})
     (aset event "preventDefault" (fn [] (reset! prevented? true)))
     (aset event "stopPropagation" (fn [] (when stopped? (reset! stopped? true))))
     (aset event "stopImmediatePropagation" (fn [] (when immediate-stopped? (reset! immediate-stopped? true))))
     event)))

(defn- mount-editable! [html]
  (let [editable (.createElement js/document "div")]
    (set! (.-className editable) "sun-editor-editable")
    (set! (.-contentEditable editable) "true")
    (set! (.-innerHTML editable) html)
    (.appendChild (.-body js/document) editable)
    editable))

(defn- mount-selected-cell-editable! [cell-class cell-html]
  (mount-editable!
   (str "<table><tbody><tr><td class=\"" cell-class "\">"
        cell-html
        "</td></tr></tbody></table>")))

(deftest single-cell-marker-paste-replaces-focused-table-cell
  (testing "single-cell marker paste replaces the selected table cell instead of appending content"
    (with-stubbed-exec-command
      (fn []
        (let [editable (mount-selected-cell-editable!
                        "se-selected-cell-focus"
                        "<div><span style=\"font-size: 20px;\"><span style=\"background-color: #b91c1c;\"><span style=\"color: rgb(74, 222, 128);\"><strong>123</strong></span></span></span></div>")
              target-cell (.querySelector editable "td")
              text-node (.-firstChild (.-firstChild target-cell))
              prevented? (atom false)
              pasted-html (str sut/single-cell-copy-marker
                               "<span style=\"font-size: 20px;\"><span style=\"background-color: #b91c1c;\"><span style=\"color: rgb(74, 222, 128);\"><strong>123</strong></span></span></span>")
              event (clipboard-event target-cell pasted-html "123" prevented?)]
          (try
            (set-collapsed-selection! text-node 1)
            (sut/maybe-normalize-paste-artifacts! event editable)
            (is @prevented?)
            (is (= "123" (str/trim (.-textContent target-cell))))
            (is (= 1 (count (re-seq #"123" (.-innerHTML target-cell)))))
            (is (str/includes? (.-innerHTML target-cell) "background-color: #b91c1c;"))
            (is (str/includes? (.-innerHTML target-cell) "color: rgb(74, 222, 128);"))
            (is (.contains (.-classList target-cell) "se-selected-cell-focus"))
            (finally
              (.remove editable))))))))

(deftest single-cell-marker-paste-replaces-table-cell-even-without-focus-class
  (testing "single-cell marker paste still replaces the whole table cell when SunEditor transiently drops the selected-cell class"
    (with-stubbed-exec-command
      (fn []
        (let [editable (mount-selected-cell-editable!
                        ""
                        "<div><span style=\"font-size: 20px;\"><span style=\"background-color: #b91c1c;\"><span style=\"color: rgb(74, 222, 128);\"><strong>123</strong></span></span></span></div>")
              target-cell (.querySelector editable "td")
              text-node (.-firstChild (.-firstChild target-cell))
              prevented? (atom false)
              stopped? (atom false)
              immediate-stopped? (atom false)
              pasted-html (str sut/single-cell-copy-marker
                               "<span style=\"font-size: 20px;\"><span style=\"background-color: #b91c1c;\"><span style=\"color: rgb(74, 222, 128);\"><strong>123</strong></span></span></span>")
              event (clipboard-event target-cell pasted-html "123" prevented? stopped? immediate-stopped?)]
          (try
            (set-collapsed-selection! text-node 1)
            (sut/maybe-normalize-paste-artifacts! event editable)
            (is @prevented?)
            (is @stopped?)
            (is @immediate-stopped?)
            (is (= "123" (str/trim (.-textContent target-cell))))
            (is (= 1 (count (re-seq #"123" (.-innerHTML target-cell)))))
            (is (str/includes? (.-innerHTML target-cell) "background-color: #b91c1c;"))
            (is (str/includes? (.-innerHTML target-cell) "color: rgb(74, 222, 128);"))
            (finally
              (.remove editable))))))))

(deftest plain-text-paste-replaces-focused-table-cell-without-marker
  (testing "plain text paste still replaces a selected/focused table cell when clipboard HTML marker is unavailable"
    (with-stubbed-exec-command
      (fn []
        (let [editable (mount-selected-cell-editable!
                        "se-selected-cell-focus"
                        "<div><strong>before</strong><br><em>after</em></div>")
              target-cell (.querySelector editable "td")
              target-node (.-firstChild (.querySelector target-cell "strong"))
              prevented? (atom false)
              event (clipboard-event target-cell "" "replacement text" prevented?)]
          (try
            (set-collapsed-selection! target-node 2)
            (sut/maybe-normalize-paste-artifacts! event editable)
            (is @prevented?)
            (is (= "replacement text" (str/trim (.-textContent target-cell))))
            (is (= 1 (count (re-seq #"replacement text" (.-innerHTML target-cell)))))
            (is (not (str/includes? (.-innerHTML target-cell) "before")))
            (finally
              (.remove editable))))))))

(deftest html-paste-replaces-se-selected-table-cell
  (testing "html paste replaces a se-selected-table-cell instead of appending to existing content"
    (with-stubbed-exec-command
      (fn []
        (let [editable (mount-selected-cell-editable!
                        "se-selected-table-cell"
                        "<div><span>old</span></div>")
              target-cell (.querySelector editable "td")
              inner-target (.querySelector target-cell "span")
              text-node (.-firstChild inner-target)
              prevented? (atom false)
              pasted-html "<span style=\"color: rgb(59, 130, 246);\"><strong>new</strong></span>"
              event (clipboard-event inner-target pasted-html "new" prevented?)]
          (try
            (set-collapsed-selection! text-node 1)
            (sut/maybe-normalize-paste-artifacts! event editable)
            (is @prevented?)
            (is (= "new" (str/trim (.-textContent target-cell))))
            (is (= 1 (count (re-seq #"new" (.-innerHTML target-cell)))))
            (is (str/includes? (.-innerHTML target-cell) "color: rgb(59, 130, 246);"))
            (is (not (str/includes? (.-innerHTML target-cell) "old")))
            (finally
              (.remove editable))))))))




