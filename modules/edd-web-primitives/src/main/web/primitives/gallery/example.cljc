(ns web.primitives.gallery.example
  #?(:cljs (:require-macros [web.primitives.gallery.example]))
  #?(:clj (:require [clojure.pprint :as pprint]
                    [clojure.string :as str])))

#?(:clj
   (defonce ^:private demo-forms (atom {})))

#?(:clj
   (defn- current-ns [env]
     (or (-> env :ns :name) (ns-name *ns*))))

#?(:clj
   (defn- form->code-string [form]
     (binding [pprint/*print-right-margin* 80
               pprint/*print-miser-width*  nil]
       (str/trimr
        (with-out-str
          (pprint/with-pprint-dispatch pprint/code-dispatch
            (pprint/pprint form)))))))

#?(:clj
   (defn- strip-map-commas
     "pprint separates map entries with ', '. Idiomatic Clojure omits them.
      Only commas outside string literals are removed."
     [s]
     (let [sb (StringBuilder.)]
       (loop [i 0 in-str? false escaped? false]
         (if (>= i (count s))
           (str sb)
           (let [c (.charAt ^String s i)]
             (cond
               escaped?
               (do (.append sb c) (recur (inc i) in-str? false))

               (and in-str? (= c \\))
               (do (.append sb c) (recur (inc i) in-str? true))

               (= c \")
               (do (.append sb c) (recur (inc i) (not in-str?) false))

               (and (not in-str?) (= c \,))
               (recur (inc i) in-str? false)

               :else
               (do (.append sb c) (recur (inc i) in-str? false)))))))))

#?(:clj
   (defn- demo-key [ns-sym sym]
     (when (and (symbol? sym) (nil? (namespace sym)))
       (symbol (str ns-sym) (name sym)))))

#?(:clj
   (defn- used-demos
     "Demo helper forms referenced (transitively) from `body`, in definition order."
     [ns-sym body]
     (let [seen (atom [])]
       (letfn [(walk [form]
                 (doseq [sym (filter symbol? (tree-seq coll? seq form))
                         :let [k (demo-key ns-sym sym)]
                         :when (and k
                                    (contains? @demo-forms k)
                                    (not (some #{k} @seen)))]
                   (swap! seen conj k)
                   (walk (get @demo-forms k))))]
         (walk body))
       (map @demo-forms (reverse @seen)))))

#?(:clj
   (defmacro defdemo
     "Like defn, but also records the source so `example` can show it.

      (defdemo counter [label]
        (let [n (r/atom 0)]
          (fn [] [RawButton {:on-click #(swap! n inc)} (str label @n)])))"
     [name & fdecl]
     (swap! demo-forms assoc
            (symbol (str (current-ns &env)) (str name))
            (list* 'defn name fdecl))
     `(defn ~name ~@fdecl)))

#?(:clj
   (defmacro example
     "Capture a demo's source and its live render in one place.

      (example {:id :contained :title \"Contained\"}
        [RawButton {:variant \"contained\"} \"Click\"])

      Expands to {:id .. :title .. :code \"<source>\" :render (fn [] <body>)}.
      Any defdemo helpers used by the body are prepended to :code, so the
      shown code is complete and copy-ready while the body is written once."
     [meta-map & body]
     (let [helpers (used-demos (current-ns &env) body)
           code    (->> (concat helpers body)
                        (map form->code-string)
                        (map strip-map-commas)
                        (str/join "\n\n"))]
       `(merge ~meta-map
               {:code   ~code
                :render (fn [] ~@body)}))))
