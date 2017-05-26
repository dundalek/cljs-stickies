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
   (update-in db [:notes id] #(merge % data))))
