(ns build
  "Builds the two published artifacts.

     clojure -T:build primitives :version '\"1.0.123\"'   ; web primitives (cljs source jar)
     clojure -T:build sdk        :version '\"1.0.123\"'   ; UI test SDK (clj source jar)
     clojure -T:build all        :version '\"1.0.123\"'

   Options: :group (maven group id), :version, :out (output dir)."
  (:require
   [clojure.tools.build.api :as b]))

(def ^:private defaults
  {:group   "com.alpha-prosoft"
   :version "1.0.0-SNAPSHOT"
   :out     "target/release-libs"})

(def ^:private artifacts
  {:primitives {:artifact "edd-web-primitives"
                :aliases  []
                :src-dirs ["src/main" "src/shared"]
                :desc     "MUI based Reagent/re-frame web primitives."}
   :sdk        {:artifact "edd-web-primitives-ui-sdk"
                :aliases  [:sdk]
                :src-dirs ["src/sdk" "src/shared"]
                :desc     "Playwright actions for edd-web-primitives, addressed by el-id."}})

(defn- opts [params]
  (merge defaults (select-keys params [:group :version :out])))

(defn- build-jar [kind params]
  (let [{:keys [group version out]}
        (opts params)

        {:keys [artifact aliases src-dirs desc]}
        (get artifacts kind)

        lib
        (symbol group artifact)

        class-dir
        (str "target/classes-" (name kind))

        jar-file
        (format "%s/%s-%s.jar" out artifact version)

        basis
        (b/create-basis {:project "deps.edn" :aliases aliases})]

    (b/delete {:path class-dir})
    (b/write-pom {:class-dir class-dir
                  :lib       lib
                  :version   version
                  :basis     basis
                  :src-dirs  src-dirs
                  :pom-data  [[:description desc]]})
    (b/copy-dir {:src-dirs   src-dirs
                 :target-dir class-dir})
    (b/jar {:class-dir class-dir
            :jar-file  jar-file})
    (b/copy-file {:src    (b/pom-path {:lib lib :class-dir class-dir})
                  :target (str jar-file ".pom.xml")})
    (println "Built" jar-file)
    jar-file))

(defn primitives [params]
  (build-jar :primitives params))

(defn sdk [params]
  (build-jar :sdk params))

(defn all [params]
  (primitives params)
  (sdk params))

;; ---------------------------------------------------------------------------
;; Integration-test uberjar
;;
;; A self-contained runnable jar with the IT suite, the SDK and
;; clojure-playwright. Runs on the ui-runtime image (Chrome + Java, no clojure
;; CLI) via `java -jar`. The gallery is served separately as a static site.
;;
;;   clojure -T:build it-uberjar :out '"/dist/it"'
;; ---------------------------------------------------------------------------

(defn it-uberjar [params]
  (let [{:keys [out]}
        (merge {:out "target/it"} (select-keys params [:out]))

        class-dir
        "target/classes-it"

        uber-file
        (str out "/it-tests.jar")

        basis
        (b/create-basis {:project "deps.edn" :aliases [:it]})]

    (b/delete {:path class-dir})
    (b/copy-dir {:src-dirs   ["src/it" "src/sdk" "src/shared"]
                 :target-dir class-dir})
    (b/compile-clj {:basis      basis
                    :ns-compile '[web.gallery.it.runner]
                    :class-dir  class-dir})
    (b/uber {:class-dir class-dir
             :uber-file uber-file
             :basis     basis
             :main      'web.gallery.it.runner})
    (println "Built" uber-file)
    uber-file))

