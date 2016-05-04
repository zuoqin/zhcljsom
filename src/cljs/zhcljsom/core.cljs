(ns zhcljsom.core
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [ajax.core :refer [GET POST]]
  )
)

(enable-console-print!)

(defn some-stories []
[
  {:Title "The title 1" :Introduction "The introduction 1" :Published "04/27/2016 - 18:38" :Reference "aHR0cDovL3d3dy56ZXJvaGVkZ2UuY29tL25ld3MvMjAxNi0wNC0yNy9lbmQtdmVuZXp1ZWxhLXJ1bnMtb3V0LW1vbmV5LXByaW50LW5ldy1tb25leQ==" :Body "The story body" } 
  {:Title "The title 2" :Introduction "The introduction 2" :Published "04/27/2017 - 18:38" :Reference "R0cDovL3d3dy56ZXJvaGVkZ2UuY29tL25ld3MvMjAxNi0wNC0yNy9lbmQtdmVuZXp1ZWxhLXJ1bnMtb3V0LW1vbmV5LXByaW50LW5ldy1tb25leQ=="}
  {:Title "The title 3" :Introduction "The introduction 3" :Published "04/27/2016 - 18:38" :Reference "HR0cDovL3d3dy56ZXJvaGVkZ2UuY29tL25ld3MvMjAxNi0wNC0yNy9lbmQtdmVuZXp1ZWxhLXJ1bnMtb3V0LW1vbmV5LXByaW50LW5ldy1tb25leQ=="}
;  "Lion" "Zebra" "Buffalo" "Antelope"
  
] 
)


(defonce app-state 
  ;; View = 0 corresponds to the items list view
  ;; View = 1 corresponds to the single item view
  (atom {:stories (some-stories) :view 0 })
)


(defn display-title [{:keys [Title Introduction] :as story}]
  (str Title)
)

(defn error-handler [{:keys [status status-text]}]
  (.log js/console (str "something bad happened: " status " " status-text)))



(defn OnDownloadStory [response]
  (
    let [     
      ;;newdata (js->clj response)
      newdata (vector {:Title (get response "Title")  :Introduction (get response "Introduction")
         :Reference (get response "Reference") :Body (get response "Body")}  
     )


;;[{:Title (get (first response) "Title") :Introduction  (get (first response) "Introduction") :Reference  (get (first response) "Reference") :Updated  (get (first response) "Updated") :Published (get (first response) "Pub;ished")}]
    ]

    (.log js/console (str newdata))
    ;;(.log js/console (str (select-keys (js->clj response) [:Title :Reference :Introduction])  ))    
    ;(swap! app-state assoc-in pageid newdata )
    (swap! app-state assoc-in [:stories] newdata )
    (swap! app-state assoc-in [:view] 1 )
  )
  
  ;;(.log js/console (str  (response) ))
  ;;(.log js/console (str  (get (first response)  "Title") ))

  
  
)


(defn downloadstory [href]
  (GET (str "http://take5people.cn:8083/api/story/" href)  {:handler OnDownloadStory
                                              :error-handler error-handler})

)


(defn story-view [story owner]
  (reify
    om/IRender
    (render [this]
      (dom/div #js  {:className "panel-primary"}
        (dom/div #js {:className "panel-heading"}
          (dom/h3 #js {:className "panel-title"}
            (dom/a #js {:href "#" :onClick (fn [e] (downloadstory (get story :Reference)))}
               (dom/div #js {:dangerouslySetInnerHTML #js {:__html (get story :Title)}} nil)
            )
          nil )
        )
        (dom/div #js {:className "panel-body"}
          (dom/div #js {:dangerouslySetInnerHTML #js {:__html (get story :Introduction)}} nil)
        )
      )
    )
  )
)


(defn handler [response]
  (
    let [
      pageid (vector (keyword (str (get response "Page" )) )) 
      ;;newdata (js->clj response)
      newdata (into []
      (map
        (fn [story]
          (assoc story
           :Title (get story "Title") :Introduction (get story "Introduction") :Reference (get story "Reference") ))
              (get response "Data")
      ))
    ]

    (.log js/console (str pageid))
    ;;(.log js/console (str (select-keys (js->clj response) [:Title :Reference :Introduction])  ))    
    ;(swap! app-state assoc-in pageid newdata )
    (swap! app-state assoc-in [:stories] newdata)
    (swap! app-state assoc-in [:view] 0 )
  )
  
  ;;(.log js/console (str  (response) ))
  ;;(.log js/console (str  (get (first response)  "Title") ))

  
  
)





(defn downloadpage [pageid]
  (GET (str "http://take5people.cn:8083/api/page/" pageid)  {:handler handler
                                              :error-handler error-handler})

)



(defn single-story-view [data]
  (dom/div #js  {:className "panel-primary"}
    (dom/div #js {:className "panel-heading"}
      (dom/h3 #js {:className "panel-title"}
        (dom/div #js {:dangerouslySetInnerHTML #js {:__html (get (first (:stories data))  :Title)  }} 
         nil)
       nil )
    )
    (dom/div #js {:className "panel-body"}
      (dom/div #js {:dangerouslySetInnerHTML #js {:__html  (get (first (:stories data))  :Body)}} nil)
    )
  )   
)

(defn item-view [data owner]
  (reify
    om/IRender
    (render [_]      
      (single-story-view data)
    )
  )
)

(defn list-view [app owner]
  (reify
    om/IRender
    (render [_]
      (dom/div #js {:className "col-md-12" :id "blogItems" :styles "margin-top: 60px;"}
        (apply dom/ul nil
          (om/build-all story-view (:stories app) {:key :Reference})
        )
      )               
    )
  )
)

(defmulti website-view (fn [data _] (:view data)))

(defmethod website-view 0
  [data owner] 
  (list-view data owner)
)

(defmethod website-view 1
  [data owner] 
  (item-view data owner)
)


(defn changeview [data]
  (->> data
    :stories
    (mapv (fn [x]
            (if (:view data)
              x)
      )
    )
  )
)

(defn main-view [app owner]
  (reify
    om/IRender
    (render [_]
      (dom/div nil
        (dom/div #js {:role "navigation", :className "navbar navbar-inverse navbar-fixed-top"}
          (dom/div #js {:className "navbar-collapse collapse"}
            (dom/div #js {:align="left"}
              (dom/ul #js {:className "nav navbar-nav"}
                (dom/li #js {:id="page0li"}
                  (dom/a #js {:href "#" :onClick (fn [e] (downloadpage 0))}
                    "Home"
                  )
                )
                (dom/li #js {:id="page1li"}
                  (dom/a #js {:href "#" :onClick (fn [e] (downloadpage 1))}
                    "Page 1"
                  )
                )
                (dom/li #js {:id="page2li"}
                  (dom/a #js {:href "#" :onClick (fn [e] (downloadpage 2))}
                    "Page 2"
                  )
                )
                (dom/li #js {:id="page3li"}
                  (dom/a #js {:href "#" :onClick (fn [e] (downloadpage 3))}
                    "Page 3"
                  )
                )
                (dom/li #js {:id="page4li"}
                  (dom/a #js {:href "#" :onClick (fn [e] (downloadpage 4))}
                    "Page 4"
                  )
                )
                (dom/li #js {:id="page5li"}
                  (dom/a #js {:href "#" :onClick (fn [e] (downloadpage 5))}
                    "Page 5"
                  )
                )
              )
            )           
          )
        )
        (om/build website-view app {:key :Reference} )
      )      
    )
  )
)



(om/root
 main-view
 app-state
 {:target (js/document.getElementById "app")})
