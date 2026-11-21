(ns web.widgets.gallery.pages.folder
  (:require
   [clojure.string :as string]
   [re-frame.core :as rf]
   [reagent.core :as r]
   [web.primitives.components :refer [RawButton]]
   [web.primitives.gallery.example :refer-macros [defdemo example]]
   [web.primitives.gallery.page :as page]
   [web.widgets.folder.events :as events]
   [web.widgets.folder.subs :as subs]
   [web.widgets.folder.views :refer [ImageField ImageList]]))

(defn- picture-url
  "A stand-in for a stored public picture, named after its file."
  [label]
  (str "data:image/svg+xml;utf8,"
       (js/encodeURIComponent
        (str "<svg xmlns='http://www.w3.org/2000/svg' width='120' height='120'>"
             "<rect width='100%' height='100%' fill='#c7d2fe'/>"
             "<text x='50%' y='50%' text-anchor='middle' font-size='14'>" label "</text></svg>"))
       "#" label))

(defn- label-of
  [url]
  (last (string/split url #"#")))

(defonce ^:private storage
  (r/atom {:mode :instant
           :folders {}}))

(defn- fkey
  [{:keys [application-id folder]}]
  [application-id folder])

(defn- entry-public?
  [spec entry]
  (not (false? (:public (first (filter #(= entry (:name %)) spec))))))

(defn- file-key
  "Where a file lies, the way the storage service lays it out; the widget reads
   the path of its url back as the stable ref."
  [[application-id folder] entry item]
  (string/join "/" (remove nil? ["private" application-id (name (:kind folder))
                                 (:id folder) (name entry) item "v1"])))

(defn- signed-url
  "A presigned GET url of a private file: its key in the path, a token that
   changes on every read so a refresh is visible, like a short-lived signature."
  [k entry item token]
  (str "https://signed.example/" (file-key k entry item) "?token=" token))

(defn- answer!
  [event result]
  (js/setTimeout #(rf/dispatch (conj event {:result result})) 50))

(defn- record-commands!
  [k commands]
  (doseq [{:keys [cmd-id attrs]} commands]
    (case cmd-id
      :folder->save
      (swap! storage assoc-in [:folders k :spec] (:spec attrs))

      :folder->add-file
      (swap! storage assoc-in [:folders k :files [(:entry attrs) (:item attrs)]]
             {:entry (:entry attrs) :item (:item attrs) :name (:file-name attrs)})

      nil)))

(defn- upload-url-result
  [k query]
  (let [spec
        (get-in @storage [:folders k :spec])

        name
        (get-in @storage [:folders k :files [(:entry query) (:item query)] :name])

        base
        {:url (str "gallery-put/" (:item query))
         :headers {"Content-Type" "image/png"}}]
    (cond-> base
      (entry-public? spec (:entry query)) (assoc :public-url (picture-url name)))))

(defn- get-result
  "Reads a folder: its files with a url each, a CDN url for public files and a
   fresh presigned url for private ones."
  [k]
  (let [spec
        (get-in @storage [:folders k :spec])

        token
        (get-in (swap! storage update-in [:folders k :get-calls] (fnil inc 0))
                [:folders k :get-calls])]
    {:files (for [{:keys [entry item name]} (vals (get-in @storage [:folders k :files]))]
              (let [public?
                    (entry-public? spec entry)]
                {:entry entry
                 :item item
                 :name name
                 :public public?
                 :status :ready
                 :url (if public? (picture-url name) (signed-url k entry item token))}))}))

(defn- fake-call
  "The storage service of the gallery: every request succeeds."
  [{:keys [commands query on-success] :as request}]
  (let [k
        (fkey (or (first commands) query request))]
    (record-commands! k commands)
    (cond
      (= :folder->get (:query-id query))
      (answer! on-success (get-result k))

      query
      (answer! on-success (upload-url-result k query))

      :else
      (answer! on-success {}))))

(defn- fake-put
  "Sends the bytes at once, holds them until released, or fails, as the
   buttons of the demo say."
  [{:keys [on-success on-failure] :as request}]
  (case (:mode @storage)
    :hold (swap! storage update :held conj request)
    :fail (js/setTimeout #(rf/dispatch (conj on-failure {:status 500})) 50)
    (js/setTimeout #(rf/dispatch on-success) 50)))

(defn- release!
  []
  (let [held
        (:held @storage)]
    (swap! storage assoc :held [] :mode :instant)
    (doseq [{:keys [on-success]} held]
      (rf/dispatch on-success))))

(rf/reg-fx ::call fake-call)
(rf/reg-fx ::put fake-put)

(def ^:private folder
  {:service :gallery-storage
   :application-id "gallery"
   :folder {:kind :room :id "gallery-room"}
   :spec (events/images-spec [:photos :cover {:name :avatar :crop {:aspect 1}}])
   :call-fx ::call
   :put-fx ::put})

(def ^:private private-folder
  {:service :gallery-storage
   :application-id "gallery"
   :folder {:kind :vault :id "gallery-vault"}
   :spec (events/images-spec [{:name :scans :public false}])
   :call-fx ::call
   :put-fx ::put})

(defn- gets-of
  [f]
  (get-in @storage [:folders (fkey f) :get-calls] 0))

(defdemo storage-controls []
  [:div {:style {:display "flex" :gap "8px" :flex-wrap "wrap" :margin-bottom "12px"}}
   [RawButton {:el-id (page/el :folder :hold) :variant "outlined"
               :on-click #(swap! storage assoc :mode :hold)}
    "Hold uploads"]
   [RawButton {:el-id (page/el :folder :release) :variant "outlined"
               :on-click release!}
    "Release uploads"]
   [RawButton {:el-id (page/el :folder :fail) :variant "outlined"
               :on-click #(swap! storage assoc :mode :fail)}
    "Fail uploads"]
   [:span {:data-testid (page/testid :folder :mode)} (str "uploads:" (name (:mode @storage)))]])

(defdemo picture-list []
  (let [value
        (r/atom (mapv picture-url ["one" "two" "three"]))

        saved
        (r/atom "none")

        save!
        (fn []
          (reset! saved "waiting")
          (rf/dispatch [::events/when-uploaded folder
                        [::events/run-callback #(reset! saved (string/join "," (map label-of @value)))]]))]
    (fn []
      (let [uploading
            @(rf/subscribe [::subs/uploading folder])]
        [:div {:style {:max-width "520px"}}
         [storage-controls]
         [ImageList (assoc folder
                           :entry :photos
                           :value @value
                           :on-add #(swap! value conj %)
                           :on-remove (fn [url] (swap! value #(vec (remove #{url} %))))
                           :on-reorder #(reset! value %))]
         [:div {:data-testid (page/testid :folder :list :readout)}
          (str "pictures:" (string/join "," (map label-of @value)))]
         [:div {:data-testid (page/testid :folder :list :gets)}
          (str "gets:" (gets-of folder))]
         [:div {:style {:display "flex" :gap "8px" :align-items "center" :margin-top "8px"}}
          [RawButton {:el-id (page/el :folder :save) :variant "contained" :on-click save!}
           (if (pos? uploading) "Save when uploaded" "Save")]
          [:span {:data-testid (page/testid :folder :list :saved)} (str "saved:" @saved)]]]))))

(defdemo picture-field []
  (let [value
        (r/atom nil)]
    (fn []
      [:div
       [ImageField (assoc folder
                          :entry :cover
                          :value @value
                          :on-change #(reset! value %))]])))

(defdemo picture-crop []
  (let [value
        (r/atom nil)]
    (fn []
      [:div
       [ImageField (assoc folder
                          :entry :avatar
                          :value @value
                          :on-change #(reset! value %))]])))

(defdemo private-list []
  (let [value
        (r/atom [])]
    (fn []
      [:div {:style {:max-width "520px"}}
       [ImageList (assoc private-folder
                         :entry :scans
                         :value @value
                         :on-add #(swap! value conj %)
                         :on-remove (fn [ref] (swap! value #(vec (remove #{ref} %)))))]
       [:div {:data-testid (page/testid :folder :private :readout)}
        (str "refs:" (string/join "," @value))]
       [:div {:data-testid (page/testid :folder :private :gets)}
        (str "gets:" (gets-of private-folder))]])))

(def ^:private props-doc
  [{:name :entry      :type "keyword" :default "—"     :doc "Entry of the folder spec the pictures go to."}
   {:name :value      :type "vector"  :default "—"     :doc "URLs of the pictures (ImageField: one URL or nil); a private entry holds stable refs, not signed urls."}
   {:name :on-add     :type "fn"      :default "—"     :doc "ImageList: gets the URL (or private ref) of each stored upload, in the order the files were chosen."}
   {:name :on-remove  :type "fn"      :default "—"     :doc "ImageList: gets the URL (or private ref) to remove."}
   {:name :on-reorder :type "fn"      :default "—"     :doc "ImageList: gets the URLs in their new order; shows move buttons."}
   {:name :on-change  :type "fn"      :default "—"     :doc "ImageField: gets the new URL (or private ref) or nil."}
   {:name :accept     :type "string"  :default "JPEG, PNG, WebP, GIF" :doc "Media types taken; checked by type and first bytes."}
   {:name :max        :type "number"  :default "12"    :doc "ImageList: most pictures."}
   {:name :max-mb     :type "number"  :default "10"    :doc "Largest file."}
   {:name :call-fx    :type "keyword" :default "edd.client call" :doc "Effect sending requests to the storage service."}
   {:name :put-fx     :type "keyword" :default "::events/put" :doc "Effect sending the bytes."}
   {:name :get-query-id :type "keyword" :default ":folder->get" :doc "Query a private entry reads its signed urls from."}
   {:name :refresh-ms :type "number"  :default "25 min" :doc "How often a private entry re-reads its signed urls before they expire."}
   {:name :texts      :type "map"     :default "—"     :doc "Overrides of default-texts."}
   {:name :crop       :type "true | map" :default "—"  :doc "true or {:aspect ratio} to crop each chosen picture before upload; defaults to the entry's :crop in the spec."}])

(defn register! []
  (page/register!
   {:id    :folder
    :group "Widgets"
    :title "Folder pictures"
    :doc   "ImageList and ImageField upload pictures into a folder of a storage service (faked here). Save with ::events/when-uploaded so that pictures still uploading are saved too; [::subs/uploading folder] counts them. A private entry (:public false) stores each file's stable ref and shows it through a presigned url read from :folder->get."
    :props props-doc
    :variants
    [(example {:id :list :title "Picture list"}
              [picture-list])

     (example {:id :field :title "One picture"}
              [picture-field])

     (example {:id :crop :title "Crop before upload"}
              [picture-crop])

     (example {:id :private :title "Private pictures"}
              [private-list])]}))
