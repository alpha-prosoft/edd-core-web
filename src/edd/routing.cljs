(ns edd.routing
  (:require
   [bidi.bidi :as bidi]
   [reitit.core :as reitit]))

(defn path-for
  [routes page & [params]]
  (apply (partial reitit/match-by-name routes page)
         params))
