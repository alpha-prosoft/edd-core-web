(ns web.widgets.upload.events
  (:require
   [re-frame.core :as rf]
   [web.widgets.upload.db :as db]
   [edd.events :as edd-events]))

(rf/reg-event-db
 ::init
 (fn [db _]
   (merge db db/default-db)))


