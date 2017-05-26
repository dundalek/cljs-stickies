(ns stickies.views
    (:require [re-frame.core :as re-frame]
              [reagent.core :as r]))

(def container-target
  #js {:drop (fn [props monitor component]
                (let [item (.getItem monitor)
                      delta (.getDifferenceFromInitialOffset monitor)
                      x (js/Math.round (+ (.-x item) (.-x delta)))
                      y (js/Math.round (+ (.-y item) (.-y delta)))
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
             :transform (str "rotate(" rotate ")")
             :top y
             :left x}}
          [:div.sticky-header name]
          [:div.sticky-content content]]))))

(defn container-component [{notes :notes connect-drop-target :connectDropTarget draggable-sticky :draggableSticky}]
  (connect-drop-target
    (r/as-element
      [:div.container
        (for [note (js->clj notes :keywordize-keys true)]
          ^{:key (:id note)} [draggable-sticky note])])))

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
            ((drag-source "note" sticky-source sticky-collect) (r/reactify-component sticky-component)))]
    (fn []
      [context-provider {:backend backend}
        [droppable-container {:notes @notes :draggable-sticky draggable-sticky}]])))
