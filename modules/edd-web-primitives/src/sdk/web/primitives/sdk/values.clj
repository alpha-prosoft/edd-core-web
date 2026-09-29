(ns web.primitives.sdk.values
  "Read the current state of any primitive by el-id, and scan a whole page
   into a nested map keyed by el-id segments.

   `reading-of` detects the primitive type from the rendered DOM and returns a
   reading map:
     text / number / percent field -> {:type :input        :value \"..\" :label \"..\"}
     checkbox                       -> {:type :checkbox     :value true  :label \"..\"}
     switch                         -> {:type :switch       :value true  :label \"..\"}
     select                         -> {:type :select       :value \"selected label\"}
     radio group                    -> {:type :radio        :value \"selected value\"}
     slider                         -> {:type :slider       :value 30.0}
     autocomplete                   -> {:type :autocomplete :value \"..\" or [\"chip\" ..]}
     anything else                  -> {:type :text         :text \"visible text\"}

   `scan` returns the same reading maps as its leaves. The detection and
   nesting logic lives in web.primitives.testing.scan, shared with the
   gallery's in-browser snapshot button."
  (:require
   [web.primitives.sdk.core :as sdk]
   [web.primitives.testing.scan :as scan]))

(defn reading-of
  "Reading map for the element carrying `el-id`, or nil when it is not present."
  [el-id]
  (let [locator
        (sdk/el el-id)]
    (when (pos? (.count locator))
      (scan/reading (.evaluate (.first locator) scan/read-value-js)))))

(defn typed-value-of
  "{:type <detected-type> :value <value>} for the element carrying `el-id`.
   For non-controls :value is the visible text. Nil when not present."
  [el-id]
  (when-let [{:keys [type value text]} (reading-of el-id)]
    {:type  type
     :value (if (= :text type) text value)}))

(defn value-of
  "Current value of a control, or visible text of anything else, for the
   element carrying `el-id`. Nil when not present."
  [el-id]
  (:value (typed-value-of el-id)))

(def ->path scan/->path)

(defn- browser-entries
  "[id reading] pairs for every el-id under `scope`, read in one page evaluate."
  [scope]
  (for [[id raw] (.evaluate (sdk/page) scan/collect-js scope)]
    [id (scan/reading raw)]))

(defn scan
  "Read every el-id under `:scope` (CSS, default whole page) and build a
   nested map keyed by the el-id path (split on \"--\"), with reading maps as
   leaves.

   Options:
     :scope    CSS to scope the scan (default: whole page)
     :ignore   predicate on the raw id string; default skips gallery chrome,
               MUI-generated ids and icon ids"
  ([] (scan {}))
  ([{:keys [scope ignore]
     :or   {ignore scan/default-ignore?}}]
   (scan/nest (browser-entries scope) ignore)))
