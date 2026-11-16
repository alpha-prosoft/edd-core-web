(ns web.widgets.lineage.views
  "Timeline of what happened to something: one row per entry with when, who
   and what changed. Styled by the MUI theme of the application using it."
  (:require [web.widgets.lineage.elements :as els]
            ["@mui/material" :refer [Box Button Skeleton Table TableBody TableCell TableHead TableRow Typography]]
            [re-frame.core :as rf]
            [reagent.core :as r]
            [web.widgets.lineage.events :as events]
            [web.widgets.lineage.subs :as subs]))

(defn format-date
  [millis]
  (.toLocaleString (js/Date. millis) js/undefined #js {:dateStyle "medium" :timeStyle "short"}))

(defn- divider
  [^js theme]
  (str "1px solid " (.. theme -palette -divider)))

(defn- changes-table
  [changes]
  [:> Table {:size "small"}
   [:> TableHead
    [:> TableRow
     [:> TableCell "Field"]
     [:> TableCell "Before"]
     [:> TableCell "After"]]]
   (into [:> TableBody]
         (for [{:keys [field-name old-value new-value]} changes]
           ^{:key field-name}
           [:> TableRow
            [:> TableCell field-name]
            [:> TableCell (str old-value)]
            [:> TableCell (str new-value)]]))])

(defn default-row
  [{:keys [kind message metadata person changes format-date]}]
  (let [date
        (format-date (:date metadata))]
    [:> Box {:component "li" :data-testid (els/entry kind)
             :sx {:display "grid" :gap 0.5 :py 1.5 :borderTop divider
                  "&:first-of-type" {:borderTop "none" :pt 0}}}
     [:> Typography {:variant "body1" :data-testid (els/message)} message]
     [:> Typography {:variant "caption" :color "text.secondary" :data-testid (els/details)}
      (if person (str date " · " person) date)]
     (when (seq changes)
       [changes-table changes])]))

(defn- entries-list
  [entries render date-format name-actor word]
  (into [:> Box {:component "ol" :sx {:listStyle "none" :m 0 :p 0}}]
        (for [[index {:keys [actor] :as entry}] (map-indexed vector entries)
              :let [worded (if word (update entry :message word) entry)
                    named (if (and name-actor actor)
                            (assoc worded :person (name-actor actor))
                            worded)]]
          ^{:key index}
          [(get render (:kind entry) default-row) (assoc named :format-date date-format)])))

(defn default-error
  [{:keys [on-retry]}]
  [:> Box {:role "alert" :data-testid (els/error)
           :sx {:display "flex" :flexWrap "wrap" :alignItems "center" :justifyContent "space-between" :gap 2}}
   [:> Typography {:variant "body2" :color "error"} "The history could not be loaded."]
   [:> Button {:size "small" :variant "outlined" :on-click on-retry} "Try again"]])

(defn Lineage
  "Props: :service (lineage service), :user-service (resolves the people,
   optional), :application-id (optional when :call-fx adds it), :call-fx (the
   application's backend call effect, default edd.client's), :scope, :slice,
   :render {kind row-component}, :empty-text, :format-date (fn [millis]),
   :render-error (component of {:response :on-retry}, shown when loading failed),
   :name-actor (fn [{:user-id :name :email}] → how the person behind an entry is shown,
   nil for nobody; default: the name, else the email), :fallback-slice (optional: the slice that is shown
   while the lineage service has no stream of :slice at all, where the history was kept before), :fallback-label
   (what is said above a history read from :fallback-slice), :keep (optional: (fn [entry]) → only the entries it
   keeps are shown, for a part of a stream), :word (optional: (fn [message]) → the message as it is shown, e.g. in
   the words the application uses now)."
  [{:keys [scope slice fallback-slice] :as props}]
  (let [refs
        (subs/refs scope slice fallback-slice)

        load!
        #(doseq [ref refs]
           (rf/dispatch [::events/load (assoc (select-keys props [:service :user-service :application-id :call-fx])
                                              :ref ref)]))]
    (r/create-class
     {:component-did-mount load!
      :reagent-render
      (fn [{:keys [render empty-text render-error name-actor fallback-label keep word] date-format :format-date}]
        (let [{:keys [status response fallback?] all-entries :entries}
              @(rf/subscribe [::subs/shown refs])

              entries
              (if keep (filterv keep all-entries) all-entries)]
          (cond
            (and fallback? (= :loaded status) (seq entries) fallback-label)
            [:> Box {:data-testid (els/fallback)}
             [:> Typography {:variant "body2" :color "text.secondary" :sx {:mb 1}} fallback-label]
             [entries-list entries render (or date-format format-date) name-actor word]]

            (= :loading status)
            [:> Skeleton {:variant "rounded" :height 56}]

            (= :failed status)
            [(or render-error default-error) {:response response :on-retry load!}]

            (empty? entries)
            [:> Typography {:variant "body2" :color "text.secondary"}
             (or empty-text "No history yet.")]

            :else
            [entries-list entries render (or date-format format-date) name-actor word])))})))
