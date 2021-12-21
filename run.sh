#!/bin/bash 

bb -i '(let [build-id 21
      lib (symbol `com.rbinternational.glms/edd-core)
      deps (read-string
            (slurp (io/file "deps.edn")))
      global (get-in deps [:deps lib])
      deps (if global
             (assoc-in deps [:deps lib] {:mvn/version (str "1." build-id)})
             deps)
      aliases [:test]
      deps (reduce
             (fn [p alias]
               (if (get-in p [:aliases alias :extra-deps lib])
                 (assoc-in p [:aliases alias :extra-deps lib]
                           {:mvn/version (str "1." build-id)})
                 p))
             deps
             aliases)]
  (spit "deps.edn" (with-out-str
                     (clojure.pprint/pprint deps))))'
