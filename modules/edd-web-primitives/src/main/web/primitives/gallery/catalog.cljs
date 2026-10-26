(ns web.primitives.gallery.catalog
  (:require
   [web.primitives.gallery.pages.autocomplete :as autocomplete]
   [web.primitives.gallery.pages.button :as button]
   [web.primitives.gallery.pages.checkbox :as checkbox]
   [web.primitives.gallery.pages.data-display :as data-display]
   [web.primitives.gallery.pages.date-picker :as date-picker]
   [web.primitives.gallery.pages.dropzone :as dropzone]
   [web.primitives.gallery.pages.fab :as fab]
   [web.primitives.gallery.pages.facilities :as facilities]
   [web.primitives.gallery.pages.feedback :as feedback]
   [web.primitives.gallery.pages.icon-button :as icon-button]
   [web.primitives.gallery.pages.icons :as icons]
   [web.primitives.gallery.pages.lab :as lab]
   [web.primitives.gallery.pages.layout :as layout]
   [web.primitives.gallery.pages.menu :as menu]
   [web.primitives.gallery.pages.navigation :as navigation]
   [web.primitives.gallery.pages.number-field :as number-field]
   [web.primitives.gallery.pages.radio :as radio]
   [web.primitives.gallery.pages.select :as select]
   [web.primitives.gallery.pages.slider :as slider]
   [web.primitives.gallery.pages.surfaces :as surfaces]
   [web.primitives.gallery.pages.switch :as switch]
   [web.primitives.gallery.pages.text-editor :as text-editor]
   [web.primitives.gallery.pages.text-field :as text-field]
   [web.primitives.gallery.pages.theme-provider :as theme-provider]
   [web.primitives.gallery.pages.transitions :as transitions]
   [web.primitives.gallery.pages.typography :as typography]))

(defn register-all!
  "Registers every built-in primitives page with the gallery registry."
  []
  (autocomplete/register!)
  (button/register!)
  (checkbox/register!)
  (data-display/register!)
  (date-picker/register!)
  (dropzone/register!)
  (fab/register!)
  (feedback/register!)
  (icon-button/register!)
  (icons/register!)
  (lab/register!)
  (layout/register!)
  (menu/register!)
  (navigation/register!)
  (number-field/register!)
  (radio/register!)
  (select/register!)
  (slider/register!)
  (surfaces/register!)
  (switch/register!)
  (text-editor/register!)
  (text-field/register!)
  (theme-provider/register!)
  (transitions/register!)
  (typography/register!)
  (facilities/register!))
