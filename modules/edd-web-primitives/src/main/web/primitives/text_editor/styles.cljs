(ns web.primitives.text-editor.styles
  "SunEditor stylesheets, shipped inside the primitives jar.

   Two layers, concatenated in this order so our fixes win:
     vendor/*.min.css      stock SunEditor CSS, copied from node_modules by
                           `make suneditor-vendor`; never edit by hand
     vendor/overrides.css  our fixes on top, one commented rule per fix

   The CSS is inlined into the JS bundle at compile time and added to the page
   the first time a text editor renders, so consumers do not copy or link the
   files themselves."
  (:require
   [shadow.resource :as resource]))

(def ^:private style-element-id "edd-web-primitives-suneditor-css")

(def ^:private css
  (str (resource/inline "./vendor/suneditor.min.css")
       "\n"
       (resource/inline "./vendor/suneditor-contents.min.css")
       "\n"
       (resource/inline "./vendor/overrides.css")))

(defn- already-installed? []
  (some? (.getElementById js/document style-element-id)))

(defn- style-element []
  (let [element
        (.createElement js/document "style")]
    (set! (.-id element) style-element-id)
    (set! (.-textContent element) css)
    element))

(defn install!
  "Add the SunEditor CSS to the document head once. Later calls do nothing."
  []
  (when-not (already-installed?)
    (.appendChild (.-head js/document) (style-element))))
