(ns edd.dev
  (:require [clojure.java.io :as io]
            [clojure.string :as string])
  (:import [java.nio.file Files FileVisitOption LinkOption Path]
           [java.security KeyFactory KeyStore]
           [java.security.cert CertificateFactory X509Certificate]
           [java.security.spec PKCS8EncodedKeySpec]
           [java.util Base64]))

(def cert-url "http://localdevhub.s3.eu-west-1.amazonaws.com/localdevhub/local/")
(def keystore-path "ext/keystore.jks")
(def keystore-password (.toCharArray "shadow-cljs"))
(def key-alias "local")

(defn download
  [file-name]
  (slurp (str cert-url file-name)))

(defn read-certificates
  [pem]
  (let [factory
        (CertificateFactory/getInstance "X.509")

        stream
        (io/input-stream (.getBytes pem))]
    (vec (.generateCertificates factory stream))))

(defn read-private-key
  [pem algorithm]
  (let [base64
        (string/replace pem #"-----[A-Z ]+-----|\s" "")

        spec
        (PKCS8EncodedKeySpec. (.decode (Base64/getDecoder) base64))]
    (.generatePrivate (KeyFactory/getInstance algorithm) spec)))

(defn download-keystore
  []
  (println "Downloading *.local.localdevhub.com certificate to" keystore-path)
  (let [chain
        (read-certificates (str (download "cert.pem")
                                (download "chain.pem")))

        algorithm
        (.getAlgorithm (.getPublicKey ^X509Certificate (first chain)))

        private-key
        (read-private-key (download "privkey.pem") algorithm)

        keystore
        (KeyStore/getInstance "JKS")]
    (.load keystore nil nil)
    (.setKeyEntry keystore key-alias private-key keystore-password (into-array X509Certificate chain))
    (io/make-parents keystore-path)
    (with-open [out (io/output-stream keystore-path)]
      (.store keystore out keystore-password))))

(defn keystore-valid?
  []
  (let [file
        (io/file keystore-path)]
    (when (.exists file)
      (let [keystore
            (KeyStore/getInstance file keystore-password)

            ^X509Certificate cert
            (.getCertificate keystore key-alias)]
        (and cert
             (try
               (.checkValidity cert)
               true
               (catch Exception _
                 false)))))))

(defn ensure-keystore
  []
  (when-not (keystore-valid?)
    (download-keystore)))

(def build-outputs
  [".clj-kondo/.cache"
   ".cpcache"
   ".shadow-cljs"
   "node_modules"
   "package.json"
   "package-lock.json"
   "resources/public/js"
   "target"
   "www/js"])

(defn delete-tree
  [^Path path]
  (when (Files/exists path (into-array LinkOption [LinkOption/NOFOLLOW_LINKS]))
    (with-open [paths (Files/walk path (into-array FileVisitOption []))]
      (doseq [p (reverse (vec (.toArray paths)))]
        (Files/deleteIfExists p)))))

(defn clean
  [_]
  (doseq [output build-outputs]
    (println "Removing" output)
    (delete-tree (.toPath (io/file output)))))

(defn -main
  [& args]
  (ensure-keystore)
  (println (str "Open https://"
                (.getName (io/file (System/getProperty "user.dir")))
                ".local.localdevhub.com:3000"))
  ((requiring-resolve 'shadow.cljs.npm-deps/main))
  (apply (requiring-resolve 'shadow.cljs.devtools.cli/-main)
         (or (seq args) ["watch" "app"])))
