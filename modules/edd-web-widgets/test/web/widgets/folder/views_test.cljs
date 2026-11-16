(ns web.widgets.folder.views-test
  (:require [cljs.test :refer-macros [deftest is]]
            [clojure.walk :as walk]
            [web.widgets.folder.elements :as els]
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
     (= [(els/images :photos) (els/image :photos 1) (els/image :photos 2) (els/add :photos) (els/input :photos)]
        (found hiccup :data-testid)))

    (is
     (= ["Remove picture 1" "Remove picture 2"]
        (found hiccup :aria-label)))))

(deftest a-full-list-takes-no-more
  (let [hiccup
        (rendered views/ImageList (assoc props :value ["https://cdn/a"] :max 1))]
    (is
     (= [true]
        (found hiccup :disabled)))))

(deftest field-replaces-its-picture
  (let [hiccup
        (rendered views/ImageField (assoc props :value "https://cdn/a" :on-change identity))]
    (is
     (= ["Picture 1"]
        (found hiccup :alt)))

    (is
     (some #{"Replace picture"} (tree-seq coll? seq hiccup)))))
