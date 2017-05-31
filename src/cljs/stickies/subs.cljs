(ns stickies.subs
    (:require-macros [reagent.ratom :refer [reaction]])
    (:require [re-frame.core :as re-frame]))

(re-frame/reg-sub
 :name
 (fn [db]
   (:name db)))

(re-frame/reg-sub
 :edit-mode
 :edit-mode)

(re-frame/reg-sub
 :notes
 (fn [db]
   (vals (:notes db))))

(re-frame/reg-sub
 :selected-note
 (fn [db]
   (when-let [selected (:selected-note db)]
     ((:notes db) selected))))
