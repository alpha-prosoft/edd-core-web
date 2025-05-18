(ns web.widgets.snackbar-alert.db)

(def default-db
  {::message            nil
   ::auto-hide-duration 3000
   ::severity           nil
   ::anchor-origin      {:vertical   "bottom"
                         :horizontal "center"}
   ::on-close-hook      nil})