(ns stickies.views
    (:require [re-frame.core :as re-frame]
              [reagent.core :as r]))

(def transform (r/atom #js{:x 0 :y 0 :k 1}))

(def container-target
  #js {:drop (fn [props monitor component]
                (let [item (.getItem monitor)
                      delta (.getDifferenceFromInitialOffset monitor)
                      scale (.-k @transform)
                      x (js/Math.round (+ (.-x item) (/ (.-x delta) scale)))
                      y (js/Math.round (+ (.-y item) (/ (.-y delta) scale)))
                      id (.-id item)]
                  (re-frame/dispatch [:update-note id {:x x :y y}])
                  js/undefined))})

(defn container-collect [connect monitor]
  #js {:connectDropTarget (.dropTarget connect)})

(def sticky-source
  #js {:beginDrag (fn [props]
                     #js {:id (.-id props)
                          :x (.-x props)
                          :y (.-y props)})})

(defn sticky-collect [connect monitor]
  #js {:connectDragSource (.dragSource connect)
       :isDragging (.isDragging monitor)})

(defn sticky-component [{:keys [id name content color rotate y x] connect-drag-source :connectDragSource is-dragging :isDragging}]
  (when (not is-dragging)
    (connect-drag-source
      (r/as-element
        [:div.sticky
          {:style
            {:background color
             :transform (str "translate(" x "px," y "px) " "rotate(" rotate ")")}}
          [:div.sticky-header name]
          [:div.sticky-content content]]))))

(defn container-component [{notes :notes connect-drop-target :connectDropTarget draggable-sticky :draggableSticky transform :transform}]
  (connect-drop-target
    (r/as-element
      [:div.container
        [:div
          {:style
            {:transform (str "translate(" (.-x transform) "px," (.-y transform) "px) " "scale(" (.-k transform) ")")}}
          (for [note (js->clj notes :keywordize-keys true)]
            ^{:key (:id note)} [draggable-sticky note])]])))

(defn main-panel []
  (let [notes (re-frame/subscribe [:notes])
        context-provider (r/adapt-react-class (.-DragDropContextProvider js/ReactDnD))
        drag-source (.-DragSource js/ReactDnD)
        drop-target (.-DropTarget js/ReactDnD)
        backend (aget js/ReactDnDHTML5Backend "default")
        droppable-container
          (r/adapt-react-class
            ((drop-target "note" container-target container-collect) (r/reactify-component container-component)))
        draggable-sticky
          (r/adapt-react-class
            ((drag-source "note" sticky-source sticky-collect) (r/reactify-component sticky-component)))
        el (.select js/d3 "#app")
        zoom (-> js/d3
                 .zoom
                 (.filter #(or
                             (= js/d3.event.type "wheel")
                             (not (re-find #"sticky" js/d3.event.target.className))))
                 (.on "zoom" (fn [] (reset! transform (.-event.transform js/d3)))))]
    (.call el zoom)
    (fn []
      [context-provider {:backend backend}
        [droppable-container {:notes @notes :draggable-sticky draggable-sticky :transform @transform}]])))
