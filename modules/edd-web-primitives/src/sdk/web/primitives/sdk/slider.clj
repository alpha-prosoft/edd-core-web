(ns web.primitives.sdk.slider
  "RawSlider. el-id lands on the slider root <span>; the value lives on
   the inner <input type=range>, which is driven with the keyboard."
  (:require
   [web.primitives.sdk.core :as sdk]))

(defn- thumb [el-id]
  (sdk/within el-id "input[type=range]"))

(defn- number [s]
  (Double/parseDouble s))

(defn value
  [el-id]
  (number (.inputValue (thumb el-id))))

(defn min-value [el-id]
  (number (.getAttribute (thumb el-id) "min")))

(defn max-value [el-id]
  (number (.getAttribute (thumb el-id) "max")))

(defn- step [el-id]
  (number (or (.getAttribute (thumb el-id) "step") "1")))

(defn- render-gap!
  "Yield to the page so a controlled component's on-change can commit and
   re-render before the next key press. Waits for one animation frame."
  []
  (.evaluate (sdk/page)
             "() => new Promise(r => requestAnimationFrame(() => requestAnimationFrame(r)))"))

(defn set-value!
  "Move the thumb to `target` one arrow key at a time, waiting for a render
   frame between presses. Stepping one key per re-render avoids the controlled
   component race where rapid presses recompute from a stale value and drop
   increments, leaving the app state behind the DOM."
  [el-id target]
  (let [input     (thumb el-id)
        step-size (step el-id)
        target    (double target)
        start     (value el-id)
        steps     (long (Math/round (/ (- target start) step-size)))
        key-name  (if (pos? steps) "ArrowRight" "ArrowLeft")]
    (.focus input)
    (dotimes [_ (Math/abs steps)]
      (.press input key-name)
      (render-gap!))
    ;; Re-correct any residual drift (a press that still failed to commit).
    (sdk/settle
     (fn []
       (let [remaining (long (Math/round (/ (- target (value el-id)) step-size)))]
         (dotimes [_ (Math/abs remaining)]
           (.press input (if (pos? remaining) "ArrowRight" "ArrowLeft"))
           (render-gap!))))
     #(value el-id)
     target)))

(defn enabled?
  [el-id]
  (.isEnabled (thumb el-id)))
