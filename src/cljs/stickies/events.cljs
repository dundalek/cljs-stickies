(ns stickies.events
    (:require [re-frame.core :as re-frame]
              [ajax.core :refer [PUT]]
              [stickies.db :as db]))

(defn handler [response]
  (.log js/console (str response)))

(defn error-handler [{:keys [status status-text]}]
  (.log js/console (str "something bad happened: " status " " status-text)))

(defn update-item [id data]
   (PUT (str "http://localhost:3030/item/" id)
      {:format :json
       :params data
       :response-format :json
       :keywords? true
       :handler handler
       :error-handler error-handler}))

(def update-item-interceptor
   (re-frame/after
      (fn [db [_ id data]]
         (update-item id (dissoc ((:notes db) id) :rotate)))))

(re-frame/reg-event-db
 :initialize-db
 (fn  [_ _]
   db/default-db))

(re-frame/reg-event-db
 :update-note
 [update-item-interceptor]
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
