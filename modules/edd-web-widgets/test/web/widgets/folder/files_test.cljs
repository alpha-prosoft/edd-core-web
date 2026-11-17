(ns web.widgets.folder.files-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [web.widgets.folder.files :as files]))

(def png-head
  [0x89 0x50 0x4E 0x47 0x0D 0x0A 0x1A 0x0A 0x00 0x00 0x00 0x0D])

(def text-head
  (mapv #(.charCodeAt % 0) "hello there!"))

(def limits
  {:accept files/default-accept
   :max-mb 10})

(deftest pictures-are-known-by-their-first-bytes
  (is
   (= ["image/jpeg" "image/png" "image/gif" "image/webp" nil nil]
      (mapv files/sniffed-type
            [[0xFF 0xD8 0xFF 0xE0]
             png-head
             (mapv #(.charCodeAt % 0) "GIF89a")
             (mapv #(.charCodeAt % 0) "RIFF\u0000\u0000\u0000\u0000WEBPVP8 ")
             (mapv #(.charCodeAt % 0) "RIFF\u0000\u0000\u0000\u0000WAVEfmt ")
             text-head]))))

(deftest a-file-is-a-picture-when-its-type-and-bytes-agree
  (testing "a picture"
    (is
     (nil?
      (files/refusal {:declared-type "image/png" :sniffed-type "image/png" :size 100} limits))))

  (testing "text, also when named or typed as a picture, or without a type"
    (is
     (= [:not-picture :not-picture :not-picture :not-picture]
        (mapv #(files/refusal % limits)
              [{:declared-type "text/plain" :sniffed-type nil :size 100}
               {:declared-type "image/png" :sniffed-type nil :size 100}
               {:declared-type "" :sniffed-type "image/png" :size 100}
               {:declared-type "image/svg+xml" :sniffed-type nil :size 100}]))))

  (testing "a picture of a type the field does not take"
    (is
     (= :not-picture
        (files/refusal {:declared-type "image/gif" :sniffed-type "image/gif" :size 100}
                       (assoc limits :accept "image/png,image/jpeg")))))

  (testing "a picture over the size"
    (is
     (= :too-big
        (files/refusal {:declared-type "image/png" :sniffed-type "image/png" :size (* 11 1024 1024)} limits)))))

(deftest accept-takes-wildcards
  (is
   (= [true true false false]
      (mapv #(files/type-accepted? "image/*, application/pdf" %)
            ["image/webp" "application/pdf" "text/plain" nil]))))

(deftest formats-name-what-accept-takes
  (is
   (= "JPEG, PNG, WebP or GIF"
      (files/formats files/default-accept)))

  (is
   (= "JPEG or PNG"
      (files/formats "image/png, image/jpeg")))

  (is
   (= "WebP"
      (files/formats "image/webp"))))
