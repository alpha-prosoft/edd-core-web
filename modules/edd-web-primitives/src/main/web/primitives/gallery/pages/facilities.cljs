(ns web.primitives.gallery.pages.facilities
  "Complex example: a list of facilities, each rendered by the same reusable
   `facility-form` component. Every field's :el-id is a plain vector of the
   field name and the facility's UUID, e.g. [:amount id], so the component can
   repeat on the page and every field stays unique and addressable."
  (:require
   [reagent.core :as r]
   [web.primitives.data-display.core :refer [RawTypography]]
   [web.primitives.gallery.example :refer-macros [defdemo example]]
   [web.primitives.gallery.page :as page]
   [web.primitives.inputs.core :refer [EddDatePicker RawButton RawCheckbox RawNumberField RawSelect]]
   [web.primitives.layout.core :refer [RawStack]]
   [web.primitives.surfaces.core :refer [RawCard]]))

(def tenors ["12 months" "24 months" "36 months"])

(def first-facility-id #uuid "0b6f1b8e-5c2a-4f7e-9a1d-2f4c8e6a1b01")

(def second-facility-id #uuid "7d3e9c41-8b2f-4a6d-b5e0-9c1f3a7d2e02")

(defdemo facility-form [id facility on-change]
  (let [update-field! (fn [k v] (on-change id (assoc facility k v)))]
    [RawCard {:el-id  [:facility id]
              :header {:title (:name facility)}}
     [RawStack {:spacing 2}
      [RawNumberField {:el-id         [:amount id]
                       :label         "Amount"
                       :default-value (:amount facility)
                       :on-change     #(update-field! :amount %)}]
      [RawSelect {:el-id     [:tenor id]
                  :options   tenors
                  :value     (:tenor facility)
                  :on-change #(update-field! :tenor %)}]
      [EddDatePicker {:el-id     [:maturity id]
                      :label     "Maturity"
                      :value     (:maturity facility)
                      :on-change #(update-field! :maturity %)}]
      [RawCheckbox {:el-id     [:secured id]
                    :label     "Secured"
                    :checked   (boolean (:secured facility))
                    :on-change #(update-field! :secured (not (:secured facility)))}]
      [RawTypography {:el-id [:summary id]}
       (str "amount:" (:amount facility)
            " tenor:" (:tenor facility)
            " secured:" (boolean (:secured facility)))]]]))

(defdemo facility-list []
  (let [facilities (r/atom (array-map
                            first-facility-id  {:name "Facility A" :amount 100000 :tenor "12 months" :maturity "2027-06-30" :secured true}
                            second-facility-id {:name "Facility B" :amount 250000 :tenor "36 months" :maturity nil :secured false}))
        set-facility! (fn [id facility] (swap! facilities assoc id facility))
        add! (fn [] (swap! facilities assoc (random-uuid)
                           {:name "New facility" :amount 0 :tenor (first tenors) :maturity nil :secured false}))]
    (fn []
      [RawStack {:spacing 2}
       (for [[id facility] @facilities]
         ^{:key id}
         [facility-form id facility set-facility!])
       [RawButton {:el-id    :add-facility
                   :variant  "outlined"
                   :on-click add!}
        "Add facility"]
       [RawTypography {:el-id :facilities-total}
        (str "total:" (reduce + (map :amount (vals @facilities))))]])))

(def ^:private props-doc
  [{:name :el-id :type "keyword | vector" :default "—"
    :doc "Per facility: [:amount id] renders as \"amount--<uuid>\"; a scan nests it back as {:amount {:<uuid> ...}}."}])

(defn register! []
  (page/register!
   {:id    :facilities
    :group "Examples"
    :title "Facilities (complex form)"
    :doc   "A list of facilities rendered by one reusable form component. Each field's :el-id is a plain vector of the field name and the facility's UUID, e.g. [:amount id], so the component can repeat and every field stays addressable. Use the el-id snapshot to see the values nested per field and facility."
    :props props-doc
    :variants
    [(example {:id :list :title "Facility list"}
              [facility-list])]}))
