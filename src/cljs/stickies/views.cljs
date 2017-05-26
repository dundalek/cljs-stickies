(ns stickies.views
    (:require [re-frame.core :as re-frame]))

(def notes
  [{:id 1 :name "aa" :content "abc" :color "#ffc" :rotate "-6deg"}
   {:id 2 :name "bb" :content "abc" :color "#cfc" :rotate "4deg"}
   {:id 3 :name "cc" :content "abc" :color "#ccf" :rotate "3deg"}
   {:id 4 :name "dd" :content "abc" :color "#fcc" :rotate "-5deg"}
   {:id 5 :name "ee" :content "abc" :color "#ffc" :rotate "5deg"}])

(defn main-panel []
  (let [name (re-frame/subscribe [:name])]
    (fn []
      [:div
        (for [{:keys [id name content color rotate]} notes]
          ^{:key id} [:div.sticky
                        {:style
                          {:background color
                           :transform (str "rotate(" rotate ")")}}
                        [:div.sticky-header name]
                        [:div.sticky-content content]])])))
