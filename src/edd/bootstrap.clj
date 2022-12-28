(ns edd.bootstrap
  (:gen-class)
  (:require [clojure.tools.logging :as log]
            [clojure.java.io :as io]
            [clojure.string :as string])
  (:import [java.io StringWriter FileOutputStream]
           [java.util.zip ZipOutputStream ZipEntry ZipInputStream]
           [java.util HashSet]
           [java.nio.file Files]
           [java.nio.file.attribute PosixFilePermission]))

(def default-zip-name "project.zip")
(def default-zip-path (str "resources/"
                           default-zip-name))

(defn create-zip
  [& [{:keys [dir zip-path]
       :or {dir "templates/project/"
            zip-path default-zip-path}}]]
  (log/info "Packanging: " dir)
  (let [dir-file (io/file dir)
        files (file-seq dir-file)]
    (with-open [zip (ZipOutputStream.
                     (FileOutputStream.
                      (io/file zip-path)))]
      (doseq [file files]
        (let [entry (-> (.toPath dir-file)
                        (.relativize
                         (.toPath file))
                        (.toString))]
          (log/info "Checking file" entry " dir?: " (.isDirectory file))
          (when-not (.isDirectory file)
            (log/info "Adding file to zip: " entry)
            (with-open [in (io/input-stream file)]
              (.putNextEntry zip (ZipEntry. entry))
              (io/copy in zip))))))))

(defn read-stream
  [stream]
  (let [sw (StringWriter.)]
    (io/copy stream sw)
    (.toString sw)))

(defn unzip
  [& [{:keys [target
              zip-file
              name-filter
              content-filter]
       :or {zip-file default-zip-name
            name-filter (fn [%] %)
            content-filter (fn [_name content] content)}}]]
  (with-open [zip (ZipInputStream. (io/input-stream
                                    (io/resource zip-file)))]
    (loop [entry (.getNextEntry zip)]
      (let [name (-> (str target "/" (.getName entry))
                     name-filter)
            content (->> (read-stream zip)
                         (content-filter name))
            file (io/file name)]
        (log/info "Extracting: " name)
        (when-let [dir (.getParentFile file)]
          (log/info "Creating dir: " (.getPath dir))
          (.mkdirs dir))
        (spit name content)
        (when (string/ends-with? name ".sh")
          (log/info "Making executable")
          (let [perms (HashSet.)]
            (.add perms PosixFilePermission/OWNER_READ)
            (.add perms PosixFilePermission/OWNER_WRITE)
            (.add perms PosixFilePermission/OWNER_EXECUTE)
            (.add perms PosixFilePermission/GROUP_READ)
            (.add perms PosixFilePermission/GROUP_EXECUTE)
            (.add perms PosixFilePermission/OTHERS_READ)
            (.add perms PosixFilePermission/OTHERS_EXECUTE)
            (Files/setPosixFilePermissions (.toPath file)
                                           perms)))
        (when-let [next (.getNextEntry zip)]
          (recur next))))))

(defn -main
  [& args]
  (let [action (first args)
        project-name (last args)
        ns-name (string/replace project-name
                                #"-"
                                "_")]
    (case action
      "create-project" (unzip {:target (second args)
                               :name-filter (fn [%]
                                              (string/replace %
                                                              #"projectname"
                                                              ns-name))
                               :content-filter (fn [name content]
                                                 (if (string/ends-with? name "index.html")
                                                   (string/replace content
                                                                   #"projectname"
                                                                   ns-name)
                                                   (string/replace content
                                                                   #"projectname"
                                                                   project-name)))})
      "package-project" (create-zip))))

(comment
  (create-zip)
  (let [project-name "bla2"
        project-ns "bllaa"]
    (unzip {:target project-name
            :filter-f (fn [%]
                        (string/replace % #"projectname" project-ns))}))
  (-main "project-1" "ns-1"))
