(ns stickies.events
    (:require [re-frame.core :as re-frame]
              [ajax.core :refer [PUT]]
              [stickies.db :as db]))

(def dirty-notes (atom {}))

(defn handler [response]
  (.log js/console (str response)))

(defn error-handler [{:keys [status status-text]}]
  (.log js/console (str "something bad happened: " status " " status-text)))

(defn update-item-request [id data]
   (PUT (str "http://localhost:3030/item/" id)
      {:format :json
       :params data
       :response-format :json
       :keywords? true
       :handler handler
       :error-handler error-handler}))

(defn flush-dirty-notes []
  (let [dirty @dirty-notes]
    (reset! dirty-notes {})
    (doseq [[id data] dirty]
      (update-item-request id data))))

(def flush-dirty-notes-throttled
  (.throttle js/_ flush-dirty-notes 5000 #js{:trailing true :leading false}))

(defn update-item [id data]
  (swap! dirty-notes assoc id data))

(def update-item-interceptor
   (re-frame/after
      (fn [db [_ id data]]
         (update-item id (dissoc ((:notes db) id) :rotate))
         (flush-dirty-notes-throttled))))

(def add-item-interceptor
   (re-frame/after
      (fn [db [_ data]]
        (let [id (:id data)]
          (update-item id (dissoc ((:notes db) id) :rotate))
          (flush-dirty-notes)))))

(def flush-dirty-interceptor
  (re-frame/after flush-dirty-notes))

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
 :add-note
 [add-item-interceptor]
 (fn  [db [_ data]]
   (assoc-in db [:notes (:id data)] data)))

(re-frame/reg-event-db
 :select-note
 [flush-dirty-interceptor]
 (fn  [db [_ id]]
   (assoc db :selected-note id)))

(re-frame/reg-event-db
 :set-notes
 (fn  [db [_ notes]]
   (->> notes
     (map #(assoc % :rotate "0"))
     (reduce #(assoc %1 (:id %2) %2) {})
     (assoc db :notes))))
