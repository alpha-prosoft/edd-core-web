(ns web.widgets.confirmation-dialog.db)

(def default-db
  {::dialog-message  ""
   ::dialog-proceed-text ""
   ::dialog-cansel-text ""
   ::show-dialog?    false
   ::on-proceed-func nil
   ::on-cancel-func nil})