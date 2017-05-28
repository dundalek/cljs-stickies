(ns stickies.db)

; (defn assign-color [item]
;   (condp re-find item
;     #"^kmx-in" "#ffc"
;     #"^kmx-output" "#cfc"
;     #"^kmx-storage" "#fcc"
;     #"^kmx-misc" "#ccf"
;     "#fcf"))

; (def src-notes
;   (->> src
;      clojure.string/split-lines
;      (map (fn [item]
;              {:name item
;               :content ""
;               :color (assign-color item)
;               :rotate "0"}))))

; (def src-notes
;   [{:name "aa" :content "abc" :color "#fcc" :rotate "-6deg"
;         {:name "bb" :content "abc" :color "#cfc" :rotate "4deg"}
;         {:name "cc" :content "abc" :color "#ccf" :rotate "3deg"}
;         {:name "dd" :content "abc" :color "#ffc" :rotate "-5deg"}
;         {:name "ee" :content "abc" :color "#cff" :rotate "5deg"}
;         {:name "ff" :content "abc" :color "#fcf" :rotate "5deg"}}])

(def src-notes [])

(def notes
  (->> src-notes
    ; (map-indexed (fn [idx item]
    ;                 (assoc item :id (inc idx)
    ;                             :y (* 150 idx)
    ;                             :x 0)))
    (reduce #(assoc %1 (:id %2) %2) {})))

(def default-db
  {:name "re-frame"
   :notes notes})
