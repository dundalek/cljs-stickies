(ns stickies.views
    (:require [re-frame.core :as re-frame]
              [reagent.core :as r]))

(def initial-transform #js{:x 200 :y 90 :k 0.35})
(def transform (r/atom initial-transform))

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
          ; [:div.sticky-content content]
          [:div.sticky-content ""]]))))

(defn container-component [{notes :notes connect-drop-target :connectDropTarget draggable-sticky :draggableSticky transform :transform}]
  (connect-drop-target
    (r/as-element
      [:div.container
        {:on-click
          (fn [ev]
            (let [scale (.-k transform)
                  x (/ (- (.-clientX ev) (.-x transform)) scale)
                  y (/ (- (.-clientY ev) (.-y transform)) scale)
                  name (js/window.prompt "Enter a new note title")]
               (when (not (clojure.string/blank? name))
                 (let [id (str (clojure.string/replace name #"\.md$" "") ".md")]
                    (re-frame/dispatch
                      [:add-note
                        {:id id
                         :name id
                         :x x
                         :y y
                         :color "#ffc"
                         :rotate "0"
                         :content ""}])))))}
        [:div
          {:style
            {:transform (str "translate(" (.-x transform) "px," (.-y transform) "px) " "scale(" (.-k transform) ")")
             :position "absolute"}}
          (for [note (js->clj notes :keywordize-keys true)]
            ^{:key (:id note)} [draggable-sticky note])]])))

(defn main-container [props]
  (let [notes (re-frame/subscribe [:notes])
        droppable-container (:droppable-container props)]
    (fn []
      [droppable-container (merge props {:notes @notes :transform @transform})])))

(defn main-panel []
  (let [context-provider (r/adapt-react-class (.-DragDropContextProvider js/ReactDnD))
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
                 (.on "zoom" (fn [] (reset! transform (.-event.transform js/d3)))))
        initial-zoom (-> (.-zoomIdentity js/d3)
                         (.translate (.-x initial-transform) (.-y initial-transform))
                         (.scale (.-k initial-transform)))]
    (.call el zoom)
    (.transform zoom el initial-zoom)
    [context-provider {:backend backend}
      [main-container {:draggable-sticky draggable-sticky :droppable-container droppable-container}]]))
