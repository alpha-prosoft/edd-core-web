(ns web.widgets.folder.views
  "Pictures kept in a folder of a storage service. The thing they belong to
   holds their URLs as its value: these fields upload new pictures and change
   the value, the application saves it and then sends ::events/retain.
   Styled by the MUI theme of the application using them."
  (:require ["@mui/icons-material/Close$default" :as CloseIcon]
            ["@mui/material" :refer [Box Button CircularProgress IconButton Typography]]
            [re-frame.core :as rf]
            [reagent.core :as r]
            [web.widgets.folder.elements :as els]
            [web.widgets.folder.events :as events]
            [web.widgets.folder.subs :as subs]))

(def default-texts
  {:add "Add pictures"
   :add-one "Upload a picture"
   :replace "Replace picture"
   :remove (fn [position] (str "Remove picture " position))
   :picture (fn [position] (str "Picture " position))
   :uploading (fn [file-name] (str "Uploading " file-name))
   :failed (fn [file-name] (str file-name " could not be uploaded."))
   :too-big (fn [file-name max-mb] (str file-name " is larger than " max-mb " MB."))
   :full (fn [max] (str "At most " max " pictures."))
   :dismiss "Dismiss"})

(def default-max-mb
  10)

(defn- text
  [texts k & args]
  (let [t
        (get (merge default-texts texts) k)]
    (if (fn? t) (apply t args) t)))

(defn- too-big?
  [^js file max-mb]
  (> (.-size file) (* max-mb 1024 1024)))

(defn- start!
  "Uploads the chosen files, each as a new item; on-url gets the URL of each
   once it is stored. Answers the files refused for their size."
  [props entry files max-mb on-url]
  (let [{refused true accepted false}
        (group-by #(too-big? % max-mb) files)]
    (doseq [file accepted]
      (rf/dispatch [::events/upload props {:entry entry
                                           :item (str (random-uuid))
                                           :file file
                                           :started (js/Date.now)
                                           :on-uploaded [::events/run-callback on-url]}]))
    (vec refused)))

(defn- tile-sx
  [size]
  {:position "relative"
   :width size
   :height size
   :borderRadius 1
   :overflow "hidden"
   :bgcolor "action.hover"})

(defn- picture
  [{:keys [url position entry size texts on-remove]}]
  [:> Box {:component "li" :sx (tile-sx size) :data-testid (els/image entry position)}
   [:> Box {:component "img" :src url :alt (text texts :picture position) :loading "lazy"
            :sx {:width "100%" :height "100%" :objectFit "cover" :display "block"}}]
   (when on-remove
     [:> IconButton {:size "small"
                     :aria-label (text texts :remove position)
                     :on-click on-remove
                     :sx {:position "absolute" :top 4 :right 4
                          :bgcolor "background.paper"
                          "&:hover" {:bgcolor "background.paper"}}}
      [:> CloseIcon {:fontSize "small"}]])])

(defn- uploading
  [{:keys [file-name status response item] :as _upload} {:keys [props entry size texts]}]
  (if (= :failed status)
    [:> Box {:component "li" :role "alert" :data-testid (els/problem entry)
             :sx (merge (tile-sx size) {:display "grid" :placeContent "center" :gap 1 :p 1 :textAlign "center"})}
     [:> Typography {:variant "caption" :color "error" :title (pr-str response)}
      (text texts :failed file-name)]
     [:> Button {:size "small" :on-click #(rf/dispatch [::events/dismiss props item])}
      (text texts :dismiss)]]
    [:> Box {:component "li" :data-testid (els/uploading entry)
             :sx (merge (tile-sx size) {:display "grid" :placeContent "center"})}
     [:> CircularProgress {:size 28 :aria-label (text texts :uploading file-name)}]]))

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

(defn- refused-note
  [refused texts max-mb]
  (when (seq refused)
    [:> Typography {:variant "body2" :color "error" :role "alert"}
     (apply str (interpose " " (map #(text texts :too-big (.-name ^js %) max-mb) refused)))]))

(defn ImageList
  "Pictures of a list entry of a folder.
   Props: the folder (:service :folder :spec :application-id :call-fx, see
   web.widgets.folder.events), :entry, :value (the URLs), :on-add (fn [url])
   for each stored upload, :on-remove (fn [url]), :max (default 12),
   :max-mb (default 10), :accept (default \"image/*\"), :size (px of a
   tile, default 112), :texts (overrides of default-texts)."
  [_props]
  (let [refused-state
        (r/atom [])]
    (fn [{:keys [entry value on-add on-remove max max-mb accept size texts] :as props
          :or {max 12 max-mb default-max-mb accept "image/*" size 112}}]
      (let [uploads
            @(rf/subscribe [::subs/uploads props entry])

            room
            (- max (count value) (count (remove #(= :failed (:status %)) uploads)))]
        [:> Box {:sx {:display "grid" :gap 1.5}}
         (into [:> Box {:component "ul" :data-testid (els/images entry)
                        :sx {:listStyle "none" :m 0 :p 0 :display "flex" :flexWrap "wrap" :gap 1}}]
               (concat
                (for [[index url] (map-indexed vector value)]
                  ^{:key url}
                  [picture {:url url :position (inc index) :entry entry :size size :texts texts
                            :on-remove (when on-remove #(on-remove url))}])
                (for [upload uploads]
                  ^{:key (:item upload)}
                  [uploading upload {:props props :entry entry :size size :texts texts}])))
         [:> Box {:sx {:display "flex" :flexWrap "wrap" :alignItems "center" :gap 1.5}}
          [chooser {:entry entry :label (text texts :add) :multiple true :accept accept
                    :disabled (<= room 0)
                    :on-files (fn [files]
                                (reset! refused-state
                                        (start! props entry (take room files) max-mb on-add)))}]
          (when (<= room 0)
            [:> Typography {:variant "body2" :color "text.secondary"} (text texts :full max)])]
         [refused-note @refused-state texts max-mb]]))))

(defn ImageField
  "One picture of an entry of a folder: an upload replaces it.
   Props: the folder, :entry, :value (its URL or nil), :on-change
   (fn [url-or-nil]), :removable? (default true), :max-mb, :accept, :size,
   :texts."
  [_props]
  (let [refused-state
        (r/atom [])]
    (fn [{:keys [entry value on-change removable? max-mb accept size texts] :as props
          :or {removable? true max-mb default-max-mb accept "image/*" size 160}}]
      (let [uploads
            @(rf/subscribe [::subs/uploads props entry])

            busy?
            (some #(= :uploading (:status %)) uploads)]
        [:> Box {:sx {:display "grid" :gap 1.5}}
         (into [:> Box {:component "ul" :data-testid (els/images entry)
                        :sx {:listStyle "none" :m 0 :p 0 :display "flex" :flexWrap "wrap" :gap 1}}]
               (concat
                (when (and value (not busy?))
                  [^{:key value}
                   [picture {:url value :position 1 :entry entry :size size :texts texts
                             :on-remove (when removable? #(on-change nil))}]])
                (for [upload uploads]
                  ^{:key (:item upload)}
                  [uploading upload {:props props :entry entry :size size :texts texts}])))
         [:div
          [chooser {:entry entry :label (text texts (if value :replace :add-one)) :multiple false :accept accept
                    :disabled busy?
                    :on-files (fn [files]
                                (reset! refused-state
                                        (start! props entry (take 1 files) max-mb on-change)))}]]
         [refused-note @refused-state texts max-mb]]))))
