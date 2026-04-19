(ns edd.i18n
  (:require [edd.db :as db]
            [re-frame.db :as re-frame-db]
            [reagent.ratom :as ratom]
            [clojure.string :as str]))

(defn deep-merge
  [a b]
  (merge-with
   (fn [v1 v2]
     (if (and (map? v1) (map? v2))
       (deep-merge v1 v2)
       v2))
   a b))

(def base-translations
  {:en {:language "English"
        :error    {:not-found-title     "Page Not Found"
                   :not-found-message   "The page you're looking for doesn't exist or has been moved."
                   :bad-request-title   "Bad Request"
                   :bad-request-message "The URL contains invalid parameters. Please check the link and try again."
                   :go-home             "Go to Homepage"}}
   :de {:language "Deutsch"
        :error    {:not-found-title     "Seite nicht gefunden"
                   :not-found-message   "Die gesuchte Seite existiert nicht oder wurde verschoben."
                   :bad-request-title   "Ungueltige Anfrage"
                   :bad-request-message "Die URL enthaelt ungueltige Parameter. Bitte ueberpruefen Sie den Link."
                   :go-home             "Zur Startseite"}}})

(def TranslationSchema [:map
                        []])

(defn- format-string
  [s params]
  (cond
    (vector? params)
    (reduce-kv
     (fn [acc idx val]
       (str/replace acc (str "{" idx "}") (str val)))
     s
     params)

    (map? params)
    (reduce-kv
     (fn [acc k v]
       (str/replace acc (str "{" (name k) "}") (str v)))
     s
     params)

    :else s))

(defn- non-reactive-db
  "Read app-db without subscribing to it. Calling `tr` inside a Reagent
   render must not couple that component to every db change — language and
   translations are effectively static at runtime, so a non-reactive read
   is correct."
  []
  (binding [ratom/*ratom-context* nil]
    @re-frame-db/app-db))

(defn tr
  [& args]
  (let [app-db (non-reactive-db)
        lang (get app-db ::db/selected-language)
        [message-spec params] (if (map? (first args))
                                [(first args) nil]
                                [args nil])
        message-key (if (map? message-spec)
                      (:message message-spec)
                      (if (keyword? (first message-spec))
                        message-spec
                        (first message-spec)))
        params (or (:params message-spec) params)
        prop (if (keyword? message-key)
               [message-key]
               message-key)
        prop (vec (concat [lang] prop))
        val (get-in (get app-db ::db/translations)
                    prop
                    (str "{tr " prop "}"))]
    (when-not (string? val)
      (throw (js/Error. (str
                         "Translation key does not result in string: "
                         (->> {:key prop
                               :value val}
                              clj->js
                              (.stringify js/JSON))))))
    (if params
      (format-string val params)
      val)))

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

