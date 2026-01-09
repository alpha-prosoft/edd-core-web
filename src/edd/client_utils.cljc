(ns edd.client-utils
  (:require
    [edd.json :as edd.json]))

(def interaction-id
  #?(:cljs (if (and (exists? js/params)
                    (.-interactionId js/params))
             (edd.json/read-uuid (.-interactionId js/params))
             (random-uuid))
     :clj  (java.util.UUID/randomUUID)))

(defn failed? [values]
  (some? (first
          (filter
           #(not= (:status %)
                  200)
           values))))

(defn map-responses [responses]
  (mapv
   (fn [r]
     {:version-id (-> r
                      (.-headers)
                      (.get "versionid"))
      :status     (.-status r)
      :body       (.json r)})
   (keep identity responses)))
