(ns web.primitives.surfaces.model
  (:require
   [re-frame.core :as rf]
   [web.primitives.db :as db]))

(rf/reg-event-db
 ::set-accordion-expanded-state
 (fn [db [_ accordion-state-id {:keys [on-expand init-expanded?] :as props}]]
   (let [state (get-in db [::db/accordion-expanded-state accordion-state-id] (boolean init-expanded?))]
     (when (contains? props :on-expand) (on-expand))
     (assoc-in db [::db/accordion-expanded-state accordion-state-id] (not (boolean state))))))

(rf/reg-sub
 ::get-accordion-expanded-state
 (fn [{:keys [::db/accordion-expanded-state]} [_ accordion-state-id {:keys [expanded? init-expanded?]}]]
   (if (some? expanded?)
     expanded?
     (get-in accordion-expanded-state [accordion-state-id] (boolean init-expanded?)))))
