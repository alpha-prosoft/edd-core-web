(ns web.primitives.gallery.pages.text-field
  (:require
   [reagent.core :as r]
   [web.primitives.gallery.example :refer-macros [defdemo example]]
   [web.primitives.gallery.page :as page]
   [web.primitives.inputs.core :refer [RawTextField]]))

(defdemo editable [variant props]
  (let [state (r/atom {:value (or (:value props) "")})]
    (fn []
      [:div
       [RawTextField
        (merge {:el-id     (page/el :text-field variant)
                :label     "Field"
                :value     (:value @state)
                :on-change #(swap! state assoc :value %)}
               (dissoc props :value))]
       [:div {:data-testid (page/testid :text-field variant :readout)}
        (str "value:" (:value @state))]])))

(def ^:private props-doc
  [{:name :value                    :type "string"  :default "—"     :doc "Controlled value."}
   {:name :on-change                :type "fn"      :default "—"     :doc "Called with the (transformed) string value."}
   {:name :transform-func           :type "fn"      :default "identity" :doc "Transforms input before on-change and display."}
   {:name :label                    :type "string"  :default "—"     :doc "Field label."}
   {:name :prefix                   :type "node"    :default "nil"   :doc "Start adornment."}
   {:name :suffix                   :type "node"    :default "nil"   :doc "End adornment."}
   {:name :read-only                :type "boolean" :default "false" :doc "Read only, no underline."}
   {:name :read-only-with-underline :type "boolean" :default "false" :doc "Read only, keeps underline."}
   {:name :disabled                 :type "boolean" :default "false" :doc "Disables the field."}
   {:name :helper-text              :type "string"  :default "nil"   :doc "Text under the field."}
   {:name :el-id                    :type "keyword | string" :default "—" :doc "Lands as id on the native input."}])

(defn register! []
  (page/register!
   {:id    :text-field
    :group "Inputs"
    :title "Text field"
    :doc   "Single-line text input. Supports adornments, transforms and read-only modes."
    :props props-doc
    :variants
    [(example {:id :basic :title "Basic"}
              [editable :basic {}])

     (example {:id :prefilled :title "Prefilled"}
              [editable :prefilled {:value "hello"}])

     (example {:id :prefix :title "With prefix"}
              [editable :prefix {:prefix "$"}])

     (example {:id :suffix :title "With suffix"}
              [editable :suffix {:suffix "kg"}])

     (example {:id :read-only :title "Read only"}
              [RawTextField {:el-id     (page/el :text-field :read-only)
                             :label     "Read only"
                             :value     "fixed"
                             :read-only true}])

     (example {:id :disabled :title "Disabled"}
              [RawTextField {:el-id    (page/el :text-field :disabled)
                             :label    "Disabled"
                             :value    "x"
                             :disabled true}])]}))
