(ns stickies.views
    (:require [re-frame.core :as re-frame]))

(def notes
  [{:id 1 :name "aa" :content "abc" :color "#ffc"}
   {:id 2 :name "bb" :content "abc" :color "#cfc"}
   {:id 3 :name "cc" :content "abc" :color "#ccf"}
   {:id 4 :name "dd" :content "abc" :color "#fcc"}
   {:id 5 :name "ee" :content "abc" :color "#ffc"}])

(defn main-panel []
  (let [name (re-frame/subscribe [:name])]
    (fn []
      [:ul
        (for [{:keys [id name content color]} notes]
          ^{:key id} [:li
                        [:a {:href "#" :style {:background color}}
                          [:h2 name]
                          [:p content]]])])))
