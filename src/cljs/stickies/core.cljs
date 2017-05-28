(ns stickies.core
    (:require [reagent.core :as reagent]
              [re-frame.core :as re-frame]
              [ajax.core :refer [GET POST]]
              [stickies.events]
              [stickies.subs]
              [stickies.views :as views]
              [stickies.config :as config]))

(defn error-handler [{:keys [status status-text]}]
  (.log js/console (str "something bad happened: " status " " status-text)))

(defn dev-setup []
  (when config/debug?
    (enable-console-print!)
    (println "dev mode")))

(defn mount-root []
  (re-frame/clear-subscription-cache!)
  (reagent/render [views/main-panel]
                  (.getElementById js/document "app")))

(defn load-data []
  (GET "http://localhost:3030/item"
    {:response-format :json
     :keywords? true
     :handler
        (fn [data]
          (js/console.log "data" data)
          (re-frame/dispatch [:set-notes data]))
     :error-handler error-handler}))

(defn ^:export init []
  (re-frame/dispatch-sync [:initialize-db])
  (dev-setup)
  (load-data)
  (mount-root))
