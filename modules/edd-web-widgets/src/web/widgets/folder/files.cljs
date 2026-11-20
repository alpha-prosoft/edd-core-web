(ns web.widgets.folder.files
  "Which chosen files are pictures. The type a browser names comes from the
   extension, so the first bytes of the file have to agree with it."
  (:require [clojure.string :as string]))

(def image-types
  ["image/jpeg" "image/png" "image/webp" "image/gif"])

(def default-accept
  (string/join "," image-types))

(def ^:private head-length
  12)

(def ^:private formats-by-type
  [["image/jpeg" "JPEG"]
   ["image/png" "PNG"]
   ["image/webp" "WebP"]
   ["image/gif" "GIF"]])

(def ^:private signatures
  {"image/jpeg" [[0 [0xFF 0xD8 0xFF]]]
   "image/png" [[0 [0x89 0x50 0x4E 0x47 0x0D 0x0A 0x1A 0x0A]]]
   "image/gif" [[0 [0x47 0x49 0x46 0x38]]]
   "image/webp" [[0 [0x52 0x49 0x46 0x46]]
                 [8 [0x57 0x45 0x42 0x50]]]})

(defn- part-matches?
  [bytes [offset expected]]
  (= expected (take (count expected) (drop offset bytes))))

(defn sniffed-type
  "The picture type the first bytes are of, nil for anything else."
  [bytes]
  (some (fn [[type parts]]
          (when (every? #(part-matches? bytes %) parts)
            type))
        signatures))

(defn- media-types
  [accept]
  (filter #(string/includes? % "/")
          (map #(string/lower-case (string/trim %))
               (string/split (str accept) #","))))

(defn type-accepted?
  "accept as in the accept attribute of a file input: media types, \"image/*\"."
  [accept type]
  (let [accepts?
        (fn [media-type]
          (if (string/ends-with? media-type "/*")
            (string/starts-with? (str type) (subs media-type 0 (dec (count media-type))))
            (= media-type type)))]
    (boolean (and (seq type) (some accepts? (media-types accept))))))

(defn formats
  "The picture formats accept takes, for messages: \"JPEG, PNG, WebP or GIF\"."
  [accept]
  (let [names
        (keep (fn [[type format-name]]
                (when (type-accepted? accept type)
                  format-name))
              formats-by-type)]
    (if (next names)
      (str (string/join ", " (butlast names)) " or " (last names))
      (first names))))

(defn refusal
  "Why a chosen file is not uploaded, nil when it is: :not-picture unless the
   type the browser names and the type of its bytes are both accepted,
   :too-big over max-mb."
  [{:keys [declared-type sniffed-type size]} {:keys [accept max-mb]}]
  (cond
    (not (and (type-accepted? accept declared-type)
              (type-accepted? accept sniffed-type)))
    :not-picture

    (> size (* max-mb 1024 1024))
    :too-big))

(defn examine
  "A promise of what the checks need to know of a chosen file."
  [^js file]
  (let [head
        (.arrayBuffer (.slice file 0 head-length))]
    (.then head
           (fn [buffer]
             {:file file
              :file-name (.-name file)
              :declared-type (.-type file)
              :sniffed-type (sniffed-type (vec (js/Array.from (js/Uint8Array. buffer))))
              :size (.-size file)}))))

(defn screen
  "A promise of the examined files, each tagged with its :reason to refuse it
   (nil when it is an accepted picture small enough), in the order given."
  [files limits]
  (.then (js/Promise.all (into-array (map examine files)))
         (fn [examined]
           (mapv #(assoc % :reason (refusal % limits)) examined))))
