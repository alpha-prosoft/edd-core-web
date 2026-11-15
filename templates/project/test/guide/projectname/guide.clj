(ns projectname.guide
  "Regenerates docs/guide from the running app:

     clojure -M:guide              every guide
     clojure -M:guide home         only these"
  (:require [projectname.guide-doc :as doc]
            [projectname.home-guide :as home]
            [web.primitives.sdk.core :as sdk]))

(def guides
  [["home" home/guide home/guide!]])

(defn- chosen
  [names]
  (if (seq names)
    (filter (fn [[guide-name]] (contains? (set names) guide-name)) guides)
    guides))

(defn -main
  [& names]
  (let [browser
        (doc/start-browser)

        results
        (atom [])]
    (try
      (sdk/with-page (:page browser)
        (doseq [[guide-name guide body-fn] (chosen names)]
          (swap! results conj [guide-name (doc/run-guide! guide body-fn)])))
      (finally
        (doc/stop-browser browser (every? second @results))))
    (println "Guides:" (pr-str @results))
    (shutdown-agents)
    (System/exit (if (every? second @results) 0 1))))
