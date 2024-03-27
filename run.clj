(require '[reitit.core :as r])
(require '[clojure.pprint :as pp])
(require '[schema.core :as s])


(def router
  (r/router
    [["/api/list/:id" :order ]
     #_["/api/details/:id" :order]]
    {:conflicts nil}))

(pp/pprint
   (r/match-by-path router "/api"))

(pp/pprint
   (r/match-by-path router "/api/ping"))


(pp/pprint 
  (r/match-by-name router :order {:id 2}))


(pp/pprint 
  (r/match-by-name router :order))

