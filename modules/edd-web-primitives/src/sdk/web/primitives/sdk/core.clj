(ns web.primitives.sdk.core
  (:require
   [clojure.string :as str]
   [web.primitives.testing.el-id :as el-id])
  (:import
   [com.microsoft.playwright Locator Locator$WaitForOptions Page]
   [com.microsoft.playwright.options WaitForSelectorState]))

(def ^:dynamic *page*
  "The Playwright Page the SDK acts on. Bind it once per test with
   `with-page`, or pass a page explicitly to `use-page!`."
  nil)

(defn- current-page ^Page []
  (or *page*
      (throw (ex-info "No page bound. Wrap the test in (sdk/with-page page ...)." {}))))

(defn page
  "The Playwright Page currently bound by `with-page`."
  ^Page []
  (current-page))

(defmacro with-page
  "Run body with `page` as the page every SDK function acts on."
  [page & body]
  `(binding [*page* ~page]
     ~@body))

(defn el-id->string
  "Resolve an el-id exactly the way components do, so tests can pass the
   same keyword / vector the view used."
  [el-id]
  (el-id/el-id el-id))

(defn by-el-id
  "CSS selector matching an element whose id or data-testid is `el-id`.
   Primitives expose el-id as :id on inputs and :data-testid on
   containers and buttons; this matches both."
  [el-id]
  (let [value
        (el-id->string el-id)]
    (str "[id='" value "'],[data-testid='" value "']")))

(defn el
  "Locator for the element carrying `el-id`."
  ^Locator [el-id]
  (.locator (current-page) (by-el-id el-id)))

(defn within
  "Locator for `css` inside the element carrying `el-id`."
  ^Locator [el-id css]
  (.locator (el el-id) css))

(defn by-text
  "Locator for visible text anywhere on the page."
  ^Locator [text]
  (.getByText (current-page) text))

(defn wait-visible
  "Wait until the element is visible and return its locator."
  ^Locator [^Locator locator]
  (.waitFor locator (-> (Locator$WaitForOptions.)
                        (.setState WaitForSelectorState/VISIBLE)))
  locator)

(defn wait-hidden
  "Wait until the element is hidden or detached."
  [^Locator locator]
  (.waitFor locator (-> (Locator$WaitForOptions.)
                        (.setState WaitForSelectorState/HIDDEN))))

(defn wait-attached
  "Wait until the element is in the DOM (it may be hidden)."
  ^Locator [^Locator locator]
  (.waitFor locator (-> (Locator$WaitForOptions.)
                        (.setState WaitForSelectorState/ATTACHED)))
  locator)

(def ^:dynamic *timeout-ms*
  "How long `eventually` and the wait helpers keep retrying."
  5000)

(defn eventually
  "Poll `f` until it returns a truthy value or *timeout-ms* passes.
   Returns the last value. MUI animates most state changes (dialogs,
   transitions, drawers, lazy editors), so read-after-act queries use this
   instead of reading the DOM once."
  [f]
  (let [deadline (+ (System/currentTimeMillis) *timeout-ms*)]
    (loop []
      (let [v (f)]
        (if (or v (> (System/currentTimeMillis) deadline))
          v
          (do (Thread/sleep 50)
              (recur)))))))

(defn eventually-false
  "Poll `f` until it returns falsy or *timeout-ms* passes. Returns the last value."
  [f]
  (not (eventually #(not (f)))))

(defn eventually=
  "Poll `read` until it returns a value equal to `expected` or *timeout-ms*
   passes. Returns the last value read. Use for read-after-act assertions so a
   re-frame / React re-render has time to land before the value is compared."
  [expected read]
  (eventually
   (fn []
     (let [v (read)]
       (when (= expected v) v))))
  (read))

(defn settle
  "Run the side-effecting action `act!`, then poll `read` until it equals
   `expected` (or *timeout-ms* passes). Returns the final value read.

   This is the building block for state-changing SDK actions: it retries the
   read so the caller never observes a value from before the UI re-rendered.
   When `read` never reaches `expected`, `act!` is re-run once per poll so a
   dropped event (e.g. a key press that raced a re-render) is repeated."
  [act! read expected]
  (act!)
  (let [deadline (+ (System/currentTimeMillis) *timeout-ms*)]
    (loop []
      (let [v (read)]
        (cond
          (= expected v)
          v

          (> (System/currentTimeMillis) deadline)
          v

          :else
          (do
            (Thread/sleep 50)
            (recur)))))))

(defn visible? [el-id]
  (.isVisible (el el-id)))

(defn becomes-visible?
  "True once the element is visible (waits up to *timeout-ms*)."
  [el-id]
  (boolean (eventually #(visible? el-id))))

(defn becomes-hidden?
  "True once the element is hidden or gone (waits up to *timeout-ms*)."
  [el-id]
  (not (eventually-false #(visible? el-id))))

(defn present? [el-id]
  (pos? (.count (el el-id))))

(defn enabled? [el-id]
  (.isEnabled (el el-id)))

(defn text
  "Trimmed visible text of the element (as rendered, so CSS
   text-transform applies, e.g. MUI buttons are upper-cased)."
  [el-id]
  (str/trim (.innerText (el el-id))))

(defn text-content
  "Trimmed text as written in the markup, ignoring CSS text-transform."
  [el-id]
  (str/trim (.textContent (el el-id))))

(defn attr [el-id attribute]
  (.getAttribute (el el-id) attribute))

(defn click! [el-id]
  (.click (el el-id)))

(defn hover! [el-id]
  (.hover (el el-id)))

(defn press! [key-name]
  (.press (.keyboard (current-page)) key-name))

(defn type-keys!
  "Type `text` one key at a time with `delay-ms` between keys, like a user."
  [text delay-ms]
  (let [keyboard
        (.keyboard (current-page))]
    (doseq [ch (seq text)]
      (.type keyboard (str ch))
      (Thread/sleep (long delay-ms)))))

(defn goto!
  "Navigate the bound page to `url`."
  [url]
  (.navigate (current-page) url))
