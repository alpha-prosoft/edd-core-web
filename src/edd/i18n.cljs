(ns edd.i18n
  (:require [re-frame.core :as rf]
            [edd.subs :as subs]))

(def base-translations
  {:language {:en "English"
              :de "Deutsch"}})

(def TranslationSchema [:map
                        []])

(defn tr
  [key]
  (let [lang @(rf/subscribe [::subs/selected-language])
        prop (if (vector? key)
               key
               [key])
        prop (vec
              (concat [lang]
                      prop))]
    (get-in @(rf/subscribe [::subs/translations])
            prop
            (str "{tr " prop "}"))))
