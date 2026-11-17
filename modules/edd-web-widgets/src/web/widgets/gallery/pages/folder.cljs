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
  "A stand-in for a stored picture, named after its file."
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
           :file-names {}
           :held []}))

(defn- answer!
  [event result]
  (js/setTimeout #(rf/dispatch (conj event {:result result})) 50))

(defn- fake-call
  "The storage service of the gallery: every request succeeds."
  [{:keys [commands query on-success]}]
  (doseq [{:keys [attrs]} (filter #(= :folder->add-file (:cmd-id %)) commands)]
    (swap! storage assoc-in [:file-names (:item attrs)] (:file-name attrs)))
  (if query
    (answer! on-success {:url (str "gallery-put/" (:item query))
                         :headers {"Content-Type" "image/png"}
                         :public-url (picture-url (get-in @storage [:file-names (:item query)]))})
    (answer! on-success {})))

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
   :spec (events/images-spec [:photos :cover])
   :call-fx ::call
   :put-fx ::put})

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

(def ^:private props-doc
  [{:name :entry      :type "keyword" :default "—"     :doc "Entry of the folder spec the pictures go to."}
   {:name :value      :type "vector"  :default "—"     :doc "URLs of the pictures (ImageField: one URL or nil)."}
   {:name :on-add     :type "fn"      :default "—"     :doc "ImageList: gets the URL of each stored upload, in the order the files were chosen."}
   {:name :on-remove  :type "fn"      :default "—"     :doc "ImageList: gets the URL to remove."}
   {:name :on-reorder :type "fn"      :default "—"     :doc "ImageList: gets the URLs in their new order; shows move buttons."}
   {:name :on-change  :type "fn"      :default "—"     :doc "ImageField: gets the new URL or nil."}
   {:name :accept     :type "string"  :default "JPEG, PNG, WebP, GIF" :doc "Media types taken; checked by type and first bytes."}
   {:name :max        :type "number"  :default "12"    :doc "ImageList: most pictures."}
   {:name :max-mb     :type "number"  :default "10"    :doc "Largest file."}
   {:name :call-fx    :type "keyword" :default "edd.client call" :doc "Effect sending requests to the storage service."}
   {:name :put-fx     :type "keyword" :default "::events/put" :doc "Effect sending the bytes."}
   {:name :texts      :type "map"     :default "—"     :doc "Overrides of default-texts."}])

(defn register! []
  (page/register!
   {:id    :folder
    :group "Widgets"
    :title "Folder pictures"
    :doc   "ImageList and ImageField upload pictures into a folder of a storage service (faked here). Save with ::events/when-uploaded so that pictures still uploading are saved too; [::subs/uploading folder] counts them."
    :props props-doc
    :variants
    [(example {:id :list :title "Picture list"}
              [picture-list])

     (example {:id :field :title "One picture"}
              [picture-field])]}))
