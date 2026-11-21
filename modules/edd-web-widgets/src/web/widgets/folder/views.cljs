(ns web.widgets.folder.views
  "Pictures kept in a folder of a storage service. The thing they belong to
   holds their URLs as its value: these fields upload new pictures and change
   the value, the application saves it once the uploads ended
   (::events/when-uploaded) and then sends ::events/retain.
   Styled by the MUI theme of the application using them."
  (:require ["@mui/icons-material/ChevronLeft$default" :as EarlierIcon]
            ["@mui/icons-material/ChevronRight$default" :as LaterIcon]
            ["@mui/icons-material/Close$default" :as CloseIcon]
            ["@mui/material" :refer [Box Button CircularProgress IconButton Typography]]
            [re-frame.core :as rf]
            [reagent.core :as r]
            [web.widgets.folder.crop :as crop]
            [web.widgets.folder.elements :as els]
            [web.widgets.folder.events :as events]
            [web.widgets.folder.files :as files]
            [web.widgets.folder.subs :as subs]))

(def default-texts
  {:add "Add pictures"
   :add-one "Upload a picture"
   :replace "Replace picture"
   :remove (fn [position] (str "Remove picture " position))
   :move-earlier (fn [position] (str "Move picture " position " earlier"))
   :move-later (fn [position] (str "Move picture " position " later"))
   :picture (fn [position] (str "Picture " position))
   :uploading (fn [file-name] (str "Uploading " file-name))
   :uploading-count (fn [n] (str "Uploading " n (if (= 1 n) " picture" " pictures") "…"))
   :failed (fn [file-name] (str file-name " could not be uploaded."))
   :not-picture (fn [file-name formats] (str file-name " is not a picture (" formats ")."))
   :too-big (fn [file-name max-mb] (str file-name " is larger than " max-mb " MB."))
   :full (fn [max] (str "At most " max " pictures."))
   :dismiss "Dismiss"})

(def default-max-mb
  10)

(def control-px
  "Smallest side of a button on a tile, big enough for a finger."
  40)

(defn- text
  [texts k & args]
  (let [t
        (get (merge default-texts texts) k)]
    (if (fn? t) (apply t args) t)))

(defn moved
  "urls with the one at index swapped with its neighbour, step -1 earlier
   and 1 later."
  [urls index step]
  (let [ordered
        (vec urls)

        target
        (+ index step)]
    (if (< -1 target (count ordered))
      (assoc ordered
             index (get ordered target)
             target (get ordered index))
      ordered)))

(defn- choose!
  "Uploads the chosen files that are pictures, each as a new item, in the
   order they were chosen; on-url gets the URL of each once it is stored,
   on-refused the others with their :reason."
  [props entry files {:keys [accept max-mb on-url on-refused]}]
  (.then (files/screen files {:accept accept :max-mb max-mb})
         (fn [checked]
           (doseq [{:keys [file file-name sniffed-type]} (remove :reason checked)]
             (rf/dispatch [::events/upload props {:entry entry
                                                  :item (str (random-uuid))
                                                  :file file
                                                  :file-name file-name
                                                  :content-type sniffed-type
                                                  :on-uploaded on-url}]))
           (on-refused (vec (filter :reason checked))))))

(defn- tile-sx
  [size]
  {:position "relative"
   :width size
   :height size
   :borderRadius 1
   :overflow "hidden"
   :bgcolor "action.hover"})

(defn- control
  [{:keys [testid label icon placement on-click]}]
  [:> IconButton {:aria-label label
                  :data-testid testid
                  :on-click on-click
                  :sx (merge {:position "absolute"
                              :width control-px
                              :height control-px
                              :bgcolor "background.paper"
                              "&:hover" {:bgcolor "background.paper"}}
                             placement)}
   [:> icon {:fontSize "small"}]])

(defn- picture
  [{:keys [url position entry size texts on-remove on-earlier on-later]}]
  [:> Box {:component "li" :sx (tile-sx size) :data-testid (els/image entry position)}
   [:> Box {:component "img" :src url :alt (text texts :picture position) :loading "lazy"
            :sx {:width "100%" :height "100%" :objectFit "cover" :display "block"}}]
   (when on-remove
     [control {:testid (els/remove-image entry position)
               :label (text texts :remove position)
               :icon CloseIcon
               :placement {:top 4 :right 4}
               :on-click on-remove}])
   (when on-earlier
     [control {:testid (els/move-earlier entry position)
               :label (text texts :move-earlier position)
               :icon EarlierIcon
               :placement {:bottom 4 :left 4}
               :on-click on-earlier}])
   (when on-later
     [control {:testid (els/move-later entry position)
               :label (text texts :move-later position)
               :icon LaterIcon
               :placement {:bottom 4 :right 4}
               :on-click on-later}])])

(defn- upload-tile
  [{:keys [file-name status response item preview-url]} {:keys [props entry size texts]}]
  (case status
    :failed
    [:> Box {:component "li" :role "alert" :data-testid (els/problem entry)
             :sx (merge (tile-sx size) {:display "grid" :placeContent "center" :gap 1 :p 1 :textAlign "center"})}
     [:> Typography {:variant "caption" :color "error" :title (pr-str response)}
      (text texts :failed file-name)]
     [:> Button {:size "small" :on-click #(rf/dispatch [::events/dismiss props item])}
      (text texts :dismiss)]]

    :stored
    [:> Box {:component "li" :data-testid (els/uploading entry) :sx (tile-sx size)}
     [:> Box {:component "img" :src preview-url :alt file-name
              :sx {:width "100%" :height "100%" :objectFit "cover" :display "block"}}]]

    [:> Box {:component "li" :data-testid (els/uploading entry)
             :sx (merge (tile-sx size) {:display "grid" :placeContent "center"})}
     [:> CircularProgress {:size 28 :aria-label (text texts :uploading file-name)}]]))

(defn- tiles
  [entry pictures uploads {:keys [props size texts]}]
  (into [:> Box {:component "ul" :data-testid (els/images entry)
                 :sx {:listStyle "none" :m 0 :p 0 :display "flex" :flexWrap "wrap" :gap 1}}]
        (concat
         (for [shown pictures]
           ^{:key (or (:ref shown) (:url shown))}
           [picture (assoc shown :entry entry :size size :texts texts)])
         (for [upload uploads]
           ^{:key (:item upload)}
           [upload-tile upload {:props props :entry entry :size size :texts texts}]))))

(defn- chooser
  "A button opening the file dialog; the input stays in the page for
   keyboards and tests, visually hidden."
  [{:keys [entry label multiple accept disabled on-files]}]
  [:> Button {:component "label" :variant "outlined" :disabled disabled
              :data-testid (els/add entry)}
   label
   [:input {:type "file"
            :accept accept
            :multiple multiple
            :data-testid (els/input entry)
            :style {:position "absolute" :width 1 :height 1 :padding 0 :margin -1
                    :overflow "hidden" :clip "rect(0 0 0 0)" :whiteSpace "nowrap" :border 0}
            :on-change (fn [^js e]
                         (let [input
                               (.-target e)

                               files
                               (vec (array-seq (.-files input)))]
                           (set! (.-value input) "")
                           (on-files files)))}]])

(defn- status
  "Always in the page, so that screen readers announce what it says."
  [entry uploads texts]
  (let [running
        (count (filter #(= :uploading (:status %)) uploads))]
    [:> Typography {:variant "body2" :color "text.secondary" :role "status"
                    :data-testid (els/status entry)}
     (when (pos? running)
       (text texts :uploading-count running))]))

(defn- refused-note
  [entry refused {:keys [texts accept max-mb]}]
  (when (seq refused)
    [:> Typography {:variant "body2" :color "error" :role "alert" :data-testid (els/refused entry)}
     (apply str (interpose " " (for [{:keys [file-name reason]} refused]
                                 (case reason
                                   :not-picture (text texts :not-picture file-name (files/formats accept))
                                   :too-big (text texts :too-big file-name max-mb)))))]))

(defn- focus-control!
  "After a move the focus stays on the moved picture, on the other button
   when it reached an end."
  [^js list-node entry index direction n]
  (let [position
        (inc index)

        shown-direction
        (cond
          (= 1 position) :later
          (= n position) :earlier
          :else direction)

        testid
        (if (= :earlier shown-direction)
          (els/move-earlier entry position)
          (els/move-later entry position))

        button
        (when list-node
          (.querySelector list-node (str "[data-testid='" testid "']")))]
    (when button
      (.focus button))))

(defn- folder-reader
  "Renders nothing; while mounted it keeps a private entry's presigned urls
   fresh (::watch reads the folder and refreshes before they expire, ::unwatch
   stops the timer). A public entry's ::watch is a no-op, so no query runs."
  [_props _entry]
  (let [current
        (atom nil)]
    (r/create-class
     {:component-did-mount
      (fn [_] (let [[props entry] @current]
                (rf/dispatch [::events/watch props entry])))

      :component-will-unmount
      (fn [_] (let [[props entry] @current]
                (rf/dispatch [::events/unwatch props entry])))

      :reagent-render
      (fn [props entry] (reset! current [props entry]) nil)})))

(defn- display-src
  "The image src for a stored value: the value itself for a public entry, the
   resolved presigned url for a private one (nil until the folder was read)."
  [private? files value]
  (if private?
    (events/signed-url files value)
    value))

(defn ImageList
  "Pictures of a list entry of a folder, in the order the files were chosen.
   Props: the folder (:service :folder :spec :application-id :call-fx, see
   web.widgets.folder.events), :entry, :value (the URLs), :on-add (fn [url])
   for each stored upload, :on-remove (fn [url]), :on-reorder (fn [urls])
   shows buttons moving a picture earlier or later, :max (default 12),
   :max-mb (default 10), :accept (default JPEG, PNG, WebP and GIF; files are
   checked by their type and first bytes), :size (px of a tile, default
   112), :texts (overrides of default-texts), :crop (true or {:aspect ratio}
   to crop each chosen picture before it is uploaded; defaults to the :crop of
   the entry in the folder spec).
   A private entry (:public false in the spec) keeps each file's stable ref as
   its value, not an expiring signed url, and shows it through the presigned
   url read from :folder->get (:get-query-id, default :folder->get; refreshed
   every :refresh-ms, default 25 min, before the url expires)."
  [_props]
  (let [refused-state
        (r/atom [])

        pending
        (r/atom [])

        list-node
        (atom nil)

        pending-focus
        (atom nil)]
    (fn [{:keys [entry value on-add on-remove on-reorder max max-mb accept size texts] :as props
          :or {max 12 max-mb default-max-mb accept files/default-accept size 112}}]
      (let [uploads
            @(rf/subscribe [::subs/uploads props entry])

            private?
            (events/private-entry? props entry)

            files
            @(rf/subscribe [::subs/files props])

            crop-cfg
            (crop/config props)

            send!
            (fn [files]
              (choose! props entry files
                       {:accept accept
                        :max-mb max-mb
                        :on-url on-add
                        :on-refused #(reset! refused-state %)}))

            guard-crop!
            (fn [files]
              (.then (files/screen files {:accept accept :max-mb max-mb})
                     (fn [checked]
                       (reset! pending (crop/to-open checked))
                       (reset! refused-state (vec (filter :reason checked))))))

            urls
            (vec value)

            n
            (count urls)

            room
            (- max n (count (remove #(= :failed (:status %)) uploads)))

            move!
            (fn [index step]
              (reset! pending-focus {:url (get urls index)
                                     :index (+ index step)
                                     :direction (if (neg? step) :earlier :later)})
              (on-reorder (moved urls index step)))

            pictures
            (map-indexed (fn [index url]
                           {:url (display-src private? files url)
                            :ref url
                            :position (inc index)
                            :on-remove (when on-remove #(on-remove url))
                            :on-earlier (when (and on-reorder (pos? index)) #(move! index -1))
                            :on-later (when (and on-reorder (< index (dec n))) #(move! index 1))})
                         urls)]
        (when-let [{:keys [url index direction]} @pending-focus]
          (when (= url (get urls index))
            (reset! pending-focus nil)
            (r/after-render #(focus-control! @list-node entry index direction n))))
        [:> Box {:sx {:display "grid" :gap 1.5} :ref #(reset! list-node %)}
         (when private?
           [folder-reader props entry])
         [tiles entry pictures uploads {:props props :size size :texts texts}]
         [:> Box {:sx {:display "flex" :flexWrap "wrap" :alignItems "center" :gap 1.5}}
          [chooser {:entry entry :label (text texts :add) :multiple true :accept accept
                    :disabled (<= room 0)
                    :on-files (fn [chosen]
                                (reset! refused-state [])
                                (let [[action files]
                                      (crop/intercept crop-cfg (take room chosen))]
                                  (case action
                                    :crop (guard-crop! files)
                                    :upload (send! files))))}]
          (when (<= room 0)
            [:> Typography {:variant "body2" :color "text.secondary"} (text texts :full max)])]
         [status entry uploads texts]
         [refused-note entry @refused-state {:texts texts :accept accept :max-mb max-mb}]
         (when-let [f (first @pending)]
           [crop/dialog {:entry entry
                         :file f
                         :crop crop-cfg
                         :texts texts
                         :on-confirm (fn [cropped]
                                       (swap! pending #(vec (rest %)))
                                       (send! [cropped]))
                         :on-cancel #(reset! pending [])}])]))))

(defn ImageField
  "One picture of an entry of a folder: an upload replaces it.
   Props: the folder, :entry, :value (its URL or nil), :on-change
   (fn [url-or-nil]), :removable? (default true), :max-mb, :accept, :size,
   :texts, :crop (true or {:aspect ratio} to crop the chosen picture before it
   is uploaded; defaults to the :crop of the entry in the folder spec).
   A private entry (:public false in the spec) keeps the file's stable ref as
   its value and shows it through the presigned url of :folder->get, like
   ImageList."
  [_props]
  (let [refused-state
        (r/atom [])

        pending
        (r/atom nil)]
    (fn [{:keys [entry value on-change removable? max-mb accept size texts] :as props
          :or {removable? true max-mb default-max-mb accept files/default-accept size 160}}]
      (let [uploads
            @(rf/subscribe [::subs/uploads props entry])

            private?
            (events/private-entry? props entry)

            files
            @(rf/subscribe [::subs/files props])

            crop-cfg
            (crop/config props)

            send!
            (fn [files]
              (choose! props entry files
                       {:accept accept
                        :max-mb max-mb
                        :on-url on-change
                        :on-refused #(reset! refused-state %)}))

            guard-crop!
            (fn [files]
              (.then (files/screen files {:accept accept :max-mb max-mb})
                     (fn [checked]
                       (reset! pending (first (crop/to-open checked)))
                       (reset! refused-state (vec (filter :reason checked))))))

            busy?
            (boolean (some #(not= :failed (:status %)) uploads))

            pictures
            (when (and value (not busy?))
              [{:url (display-src private? files value)
                :ref value
                :position 1
                :on-remove (when removable? #(on-change nil))}])]
        [:> Box {:sx {:display "grid" :gap 1.5}}
         (when private?
           [folder-reader props entry])
         [tiles entry pictures uploads {:props props :size size :texts texts}]
         [:div
          [chooser {:entry entry :label (text texts (if value :replace :add-one)) :multiple false :accept accept
                    :disabled busy?
                    :on-files (fn [chosen]
                                (reset! refused-state [])
                                (let [[action files]
                                      (crop/intercept crop-cfg (take 1 chosen))]
                                  (case action
                                    :crop (guard-crop! files)
                                    :upload (send! files))))}]]
         [status entry uploads texts]
         [refused-note entry @refused-state {:texts texts :accept accept :max-mb max-mb}]
         (when-let [f @pending]
           [crop/dialog {:entry entry
                         :file f
                         :crop crop-cfg
                         :texts texts
                         :on-confirm (fn [cropped]
                                       (reset! pending nil)
                                       (send! [cropped]))
                         :on-cancel #(reset! pending nil)}])]))))
