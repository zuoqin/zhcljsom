(ns zhcljsom.core
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [ajax.core :refer [GET POST]]
  )
)

(enable-console-print!)

(defn some-stories []
[
  {:Title "The title 1" :Introduction "The introduction 1" :Published "04/27/2016 - 18:38" :Reference "aHR0cDovL3d3dy56ZXJvaGVkZ2UuY29tL25ld3MvMjAxNi0wNC0yNy9lbmQtdmVuZXp1ZWxhLXJ1bnMtb3V0LW1vbmV5LXByaW50LW5ldy1tb25leQ=="} 
  {:Title "The title 2" :Introduction "The introduction 2" :Published "04/27/2017 - 18:38" :Reference "R0cDovL3d3dy56ZXJvaGVkZ2UuY29tL25ld3MvMjAxNi0wNC0yNy9lbmQtdmVuZXp1ZWxhLXJ1bnMtb3V0LW1vbmV5LXByaW50LW5ldy1tb25leQ=="}
  {:Title "The title 3" :Introduction "The introduction 3" :Published "04/27/2016 - 18:38" :Reference "HR0cDovL3d3dy56ZXJvaGVkZ2UuY29tL25ld3MvMjAxNi0wNC0yNy9lbmQtdmVuZXp1ZWxhLXJ1bnMtb3V0LW1vbmV5LXByaW50LW5ldy1tb25leQ=="}
;  "Lion" "Zebra" "Buffalo" "Antelope"
  
] 
)




(defonce app-state (atom {:stories  (some-stories)}))


(defn display-title [{:keys [Title Introduction] :as story}]
  (str Title)
)




(defn story-view [story owner]
  (reify
    om/IRender
    (render [this]
      (dom/div #js  {:className "panel-primary"}
        (dom/div #js {:className "panel-heading"}
          (dom/h3 #js {:className "panel-title"}
            (dom/a #js {:href (str "../story" (get story :Reference))}
              (dom/div #js {:dangerouslySetInnerHTML #js {:__html (get story :Title)}} nil)
            nil )
          nil )
        )
        (dom/div #js {:className "panel-body"}
          (dom/div #js {:dangerouslySetInnerHTML #js {:__html (get story :Introduction)}} nil)
        )
      )
    )
  )
)

(defn error-handler [{:keys [status status-text]}]
  (.log js/console (str "something bad happened: " status " " status-text)))

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





;;[{:Title (get (first response) "Title") :Introduction  (get (first response) "Introduction") :Reference  (get (first response) "Reference") :Updated  (get (first response) "Updated") :Published (get (first response) "Pub;ished")}]
    ]

    (.log js/console (str pageid))
    ;;(.log js/console (str (select-keys (js->clj response) [:Title :Reference :Introduction])  ))    
    ;(swap! app-state assoc-in pageid newdata )
    (swap! app-state assoc-in [:stories] newdata )
  )
  
  ;;(.log js/console (str  (response) ))
  ;;(.log js/console (str  (get (first response)  "Title") ))

  
  
)

(defn downloadpage [pageid]
  (GET (str "http://localhost:8083/api/page/" pageid)  {:handler handler
                                              :error-handler error-handler})

)


(defn stripe [story bgc]0
  (let [st #js {:backgroundColor bgc}]
    (dom/li #js {:style st} (get story :Title))))

(defn stories-view [app owner]
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

        (dom/div #js {:className "col-md-12" :id "blogItems" :styles "margin-top: 60px;"}
          (apply dom/ul nil
            (om/build-all story-view (:stories app) {:key :Reference})
          )
        )         
      )
      ;(apply dom/ul #js {:className "animals"} 
        ;(map story-view (:stories app) )
      ;)

    )
  )
)





(om/root
 stories-view
 app-state
 {:target (js/document.getElementById "app")})
