(ns stickies.events
    (:require [re-frame.core :as re-frame]
              [stickies.db :as db]))

(re-frame/reg-event-db
 :initialize-db
 (fn  [_ _]
   db/default-db))

(re-frame/reg-event-db
 :update-note
 (fn  [db [_ id data]]
   (let [next-db (update-in db [:notes id] #(merge % data))]
      ; (js/console.log (prn-str (vals (:notes next-db))))
      next-db)))

(re-frame/reg-event-db
 :set-notes
 (fn  [db [_ notes]]
   (->> notes
     (map #(assoc % :rotate "0"))
     (reduce #(assoc %1 (:id %2) %2) {})
     (assoc db :notes))))
