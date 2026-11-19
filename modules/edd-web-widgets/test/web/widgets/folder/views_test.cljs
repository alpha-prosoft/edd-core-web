(ns web.widgets.folder.views-test
  (:require [cljs.test :refer-macros [deftest is]]
            [clojure.walk :as walk]
            [re-frame.db :as rf-db]
            [web.widgets.folder.db :as db]
            [web.widgets.folder.elements :as els]
            [web.widgets.folder.files :as files]
            [web.widgets.folder.views :as views]))

(def props
  {:service :storage-svc
   :folder {:kind :room :id "views-room"}
   :entry :photos})

(defn- expanded
  "Hiccup with the components inside it rendered."
  [hiccup]
  (cond
    (and (vector? hiccup) (fn? (first hiccup)))
    (expanded (apply (first hiccup) (rest hiccup)))

    (vector? hiccup)
    (mapv expanded hiccup)

    (seq? hiccup)
    (map expanded hiccup)

    :else
    hiccup))

(defn- rendered
  [component props]
  (expanded ((component props) props)))

(defn- found
  [hiccup k]
  (let [values
        (atom [])]
    (walk/postwalk (fn [x]
                     (when (and (map? x) (contains? x k))
                       (swap! values conj (get x k)))
                     x)
                   hiccup)
    @values))

(deftest list-shows-each-picture-with-a-way-to-remove-it
  (let [hiccup
        (rendered views/ImageList (assoc props
                                         :value ["https://cdn/a" "https://cdn/b"]
                                         :on-remove identity))]
    (is
     (= [(els/images :photos)
         (els/image :photos 1) (els/remove-image :photos 1)
         (els/image :photos 2) (els/remove-image :photos 2)
         (els/add :photos) (els/input :photos) (els/status :photos)]
        (found hiccup :data-testid)))

    (is
     (= ["Remove picture 1" "Remove picture 2"]
        (found hiccup :aria-label)))))

(deftest list-moves-pictures-earlier-and-later
  (let [hiccup
        (rendered views/ImageList (assoc props
                                         :value ["https://cdn/a" "https://cdn/b" "https://cdn/c"]
                                         :on-reorder identity))]
    (is
     (= ["Move picture 1 later" "Move picture 2 earlier" "Move picture 2 later" "Move picture 3 earlier"]
        (found hiccup :aria-label))))

  (is
   (= [["b" "a" "c"] ["a" "c" "b"] ["a" "b" "c"] ["a" "b" "c"]]
      [(views/moved ["a" "b" "c"] 1 -1)
       (views/moved ["a" "b" "c"] 1 1)
       (views/moved ["a" "b" "c"] 0 -1)
       (views/moved ["a" "b" "c"] 2 1)])))

(deftest tile-buttons-fit-a-finger
  (let [hiccup
        (rendered views/ImageList (assoc props :value ["https://cdn/a"] :on-remove identity))

        button-sx
        (first (filter #(= "absolute" (:position %)) (found hiccup :sx)))]
    (is
     (<= 40 (:width button-sx)))

    (is
     (<= 40 (:height button-sx)))))

(def uploads-db
  (reduce (fn [db upload]
            (assoc-in db (db/upload-path props (:item upload)) (assoc upload :entry :photos)))
          {}
          [{:item "a" :order 0 :file-name "notes.png" :status :failed}
           {:item "b" :order 1 :file-name "hall.jpg" :status :uploading}
           {:item "c" :order 2 :file-name "yard.jpg" :status :uploading}]))

(deftest every-message-names-the-file
  (reset! rf-db/app-db uploads-db)
  (let [hiccup
        (rendered views/ImageList (assoc props :value []))

        texts
        (set (filter string? (tree-seq coll? seq hiccup)))]
    (reset! rf-db/app-db {})

    (is
     (contains? texts "notes.png could not be uploaded."))

    (is
     (= ["Uploading hall.jpg" "Uploading yard.jpg"]
        (found hiccup :aria-label)))

    (is
     (contains? texts "Uploading 2 pictures…")))

  (is
   (= "notes.txt is not a picture (JPEG, PNG, WebP or GIF)."
      ((:not-picture views/default-texts) "notes.txt" (files/formats files/default-accept)))))

(deftest a-full-list-takes-no-more
  (let [hiccup
        (rendered views/ImageList (assoc props :value ["https://cdn/a"] :max 1))]
    (is
     (= [true]
        (found hiccup :disabled)))))

(deftest crop-leaves-the-controls-and-shows-no-dialog-until-a-pick
  (let [hiccup
        (rendered views/ImageField (assoc props :value nil :on-change identity :crop {:aspect 1}))

        testids
        (set (found hiccup :data-testid))]
    (is
     (contains? testids (els/add :photos)))

    (is
     (contains? testids (els/input :photos)))

    (is
     (not
      (contains? testids (els/crop-dialog :photos))))))

(deftest field-replaces-its-picture
  (let [hiccup
        (rendered views/ImageField (assoc props :value "https://cdn/a" :on-change identity))]
    (is
     (= ["Picture 1"]
        (found hiccup :alt)))

    (is
     (some #{"Replace picture"} (tree-seq coll? seq hiccup)))))
