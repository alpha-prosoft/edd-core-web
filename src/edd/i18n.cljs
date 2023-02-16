(ns edd.i18n
  (:require [re-frame.core :as rf]
            [edd.subs :as subs]))

(def base-translations
  {:language {:en "English"
              :de "Deutsch"}})

(def TranslationSchema [:map
                        []])

(defn tr
  [& key]
  (let [lang @(rf/subscribe [::subs/selected-language])
        prop (if (keyword?
                  (first
                   key))
               key
               (first key))
        prop (vec
              (concat [lang]
                      prop))
        val (get-in @(rf/subscribe [::subs/translations])
            prop
            (str "{tr " prop "}"))]
    (when-not (string? val)
      (throw (js/Error. (str
                        "Translation key does not result in string: "
                        (->> {:key prop
                             :value val}
                            clj->js
                            (.stringify js/JSON))))))
    val))

(comment
  (defn convert-structure
    [in out path]
    (reduce
     (fn [out-p v]
       (let [value (get in v)
             current-path (conj path v)
             new-path (vec
                       (cons v path))]
         (println "Processing: " current-path new-path out-p value)
         (if (map? value)
           (convert-structure value
                              out-p
                              current-path)
           (assoc-in out-p
                     new-path
                     value))))
     out
     (keys in)))
  (clojure.pprint/pprint
   (convert-structure (tr)
                      {}
                      [])))

