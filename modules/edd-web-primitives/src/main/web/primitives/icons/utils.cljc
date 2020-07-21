(ns web.primitives.icons.utils
  (:require [clojure.string :as str]))

(def icon-style
  {:width "24px" :height "24px" :viewbox "0 0 24 24" :version "1.1" :xlmns "http://www.w3.org/2000/svg"})

(defn handle-props [props]
  (merge {:style (merge icon-style (:style props))} (dissoc props :style)))

(defn concat-class-names [class-names]
  (->> class-names
       (remove nil?)
       (str/join " ")))
