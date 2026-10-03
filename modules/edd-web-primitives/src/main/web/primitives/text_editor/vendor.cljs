(ns web.primitives.text-editor.vendor
  "SunEditor, shipped inside the primitives jar as its prebuilt browser bundle.

   The stock bundle (vendor/suneditor.min.js, copied from node_modules by
   `make suneditor-vendor`) is inlined into the JS output at compile time and
   evaluated once, the first time a text editor renders. Loading it this way
   keeps suneditor's source out of the Closure Compiler, which crashes on its
   private class fields in newer releases, and the bundle is already built for
   modern browsers.

   The bundle registers `window.SUNEDITOR` = {create, plugins, modules, ...}."
  (:require
   [shadow.resource :as resource]))

(def ^:private bundle
  (resource/inline "./vendor/suneditor.min.js"))

(defn- installed-api []
  (unchecked-get js/window "SUNEDITOR"))

(defn- run-bundle! []
  ((js/Function. bundle)))

(defn api
  "The SunEditor API (`create`, `plugins`, ...), loading the bundle on first use."
  []
  (or (installed-api)
      (do (run-bundle!)
          (installed-api))))
