(ns web.widgets.gallery.catalog
  (:require
   [web.widgets.gallery.pages.confirmation-dialog :as confirmation-dialog]
   [web.widgets.gallery.pages.content-switcher :as content-switcher]
   [web.widgets.gallery.pages.login :as login]
   [web.widgets.gallery.pages.snackbar-alert :as snackbar-alert]
   [web.widgets.gallery.pages.translation :as translation]
   [web.widgets.gallery.pages.upload :as upload]))

(defn register-all!
  "Registers every widget page with the gallery registry."
  []
  (confirmation-dialog/register!)
  (content-switcher/register!)
  (login/register!)
  (snackbar-alert/register!)
  (translation/register!)
  (upload/register!))
