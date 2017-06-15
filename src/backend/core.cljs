(ns stickies-backend.core
  (:require [cljs.nodejs :as nodejs]
            [cljs.tools.cli :refer [parse-opts]]))

(nodejs/enable-util-print!)

(def port 3030)
(def dir "./data")

(def fs (js/require "mz/fs"))
(def fm (js/require "front-matter"))
(def yaml (js/require "js-yaml"))
(def express (js/require "express"))
(def cors (js/require "cors"))
(def body-parser (js/require "body-parser"))
(def app (express))

(-> app
  (.use (cors))
  (.use (.json body-parser)))

; (def notes-map
;   (->> src-notes
;     (map #(select-keys % [:name :color :x :y]))
;     (reduce #(assoc %1 (:name %2) %2) {})))

(defn load-item [dir name]
    (-> (.readFile fs (str dir "/" name) "utf-8")
        (.then
          (fn [content]
            (let [parsed (fm content)]
              (.assign js/Object (.-attributes parsed)
                ; (clj->js (notes-map name))
                #js {:id name
                     :name name
                     :content (.-body parsed)}))))))

(defn save-item [dir data]
  (let [content (.-content data)
        id (.-id data)
        copied (js/Object.assign #js{} data)]
    (js-delete copied "id")
    (js-delete copied "content")
    (js-delete copied "name")
    (.writeFile fs (str dir "/" id)
      (str "---\n" (.dump yaml copied) "---\n" content))))

(defn load-items [dir]
  (-> (.readdir fs dir)
      (.then (fn [data]
               (->> data
                 js->clj
                 (filter #(not= ".git" %))
                 (map #(load-item dir %))
                 to-array
                 (js/Promise.all))))))

(.get app "/item"
  (fn [req res]
    (-> (load-items dir)
        (.then #(.send res %))
        (.catch (fn [err]
                  (-> res
                    (.status 500)
                    (.send err)))))))

(.put app "/item/:id"
  (fn [req res]
    (-> (save-item dir (.-body req))
        (.then #(.send res #js {:result "ok"}))
        (.catch (fn [err]
                  (-> res
                    (.status 500)
                    (.send err)))))))

; (def options-spec
;   [["-h" "--help"]])
;
; (defn run [args]
;   (parse-opts args options-spec))

(defn -main []
  ; (-> (load-items dir)
  ;     (.then
  ;       (fn [items]
  ;         (js/Promise.all (.map items #(save-item dir %)))))
  ;     (.then #(println "done"))
  ;     (.catch #(println "error" %))))

  ; (println (run (.-argv nodejs/process))))
  (.listen app port
      #(println "Example app listening on port X " port)))

(set! *main-cli-fn* -main)
