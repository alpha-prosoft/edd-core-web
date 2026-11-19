(ns web.widgets.folder.crop-test
  (:require [cljs.test :refer-macros [deftest is testing async]]
            [web.widgets.folder.crop :as crop]
            [web.widgets.folder.events :as events]
            [web.widgets.folder.files :as files]))

(defn- source-file
  [name type]
  (js/File. #js ["x"] name #js {:type type}))

(defn- drawable
  "A drawn canvas standing in for a loaded <img>: it carries naturalWidth and
   naturalHeight the way percent-crop->blob reads them and is a valid source
   for drawImage."
  [w h]
  (let [canvas
        (.createElement js/document "canvas")

        ctx
        (.getContext canvas "2d")]
    (set! (.-width canvas) w)
    (set! (.-height canvas) h)
    (set! (.-fillStyle ctx) "#f00")
    (.fillRect ctx 0 0 w h)
    (set! (.-naturalWidth canvas) w)
    (set! (.-naturalHeight canvas) h)
    canvas))

(def ^:private full-crop
  #js {:x 0 :y 0 :width 100 :height 100 :unit "%"})

(deftest crop-is-read-from-the-prop-or-the-spec
  (let [spec
        (events/images-spec [:photos {:name :avatar :crop {:aspect 1}}])]

    (testing "an entry with :crop in the spec"
      (is
       (= {:aspect 1}
          (crop/config {:spec spec :entry :avatar}))))

    (testing "an entry without :crop"
      (is
       (nil?
        (crop/config {:spec spec :entry :photos}))))

    (testing "the :crop prop wins over the spec"
      (is
       (true?
        (crop/config {:spec spec :entry :photos :crop true}))))))

(deftest the-aspect-is-only-for-a-fixed-crop
  (is
   (= 1.5
      (crop/aspect {:aspect 1.5})))

  (is
   (nil?
    (crop/aspect true))))

(deftest cropping-on-opens-the-dialog-off-uploads-now
  (testing "with cropping on the chosen files wait for the dialog"
    (is
     (= [:crop ["a" "b"]]
        (crop/intercept {:aspect 1} ["a" "b"]))))

  (testing "with cropping off they are uploaded straight away"
    (is
     (= [:upload ["a" "b"]]
        (crop/intercept nil ["a" "b"])))))

(deftest a-cropped-picture-keeps-the-source-type
  (async done
         (-> (crop/percent-crop->blob (drawable 20 20) full-crop (source-file "face.png" "image/png"))
             (.then (fn [^js f]
                      (is
                       (instance? js/File f))

                      (is
                       (= "image/png"
                          (.-type f)))

                      (is
                       (= "face.png"
                          (.-name f)))

                      (done)))
             (.catch (fn [e] (is (nil? e)) (done))))))

(deftest a-blank-source-type-falls-back-to-png
  (async done
         (-> (crop/percent-crop->blob (drawable 16 16) full-crop (source-file "scan" ""))
             (.then (fn [^js f]
                      (is
                       (= "image/png"
                          (.-type f)))

                      (done)))
             (.catch (fn [e] (is (nil? e)) (done))))))

(deftest a-cropped-picture-still-passes-the-first-byte-check
  (async done
         (-> (crop/percent-crop->blob (drawable 24 24) full-crop (source-file "face.png" "image/png"))
             (.then files/examine)
             (.then (fn [{:keys [sniffed-type declared-type]}]
                      (is
                       (= "image/png"
                          sniffed-type))

                      (is
                       (nil?
                        (files/refusal {:declared-type declared-type
                                        :sniffed-type sniffed-type
                                        :size 100}
                                       {:accept files/default-accept :max-mb 10})))

                      (done)))
             (.catch (fn [e] (is (nil? e)) (done))))))
