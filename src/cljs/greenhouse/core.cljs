(ns greenhouse.core
    (:require [reagent.core :as reagent :refer [atom]]
              [reagent.session :as session]
              [secretary.core :as secretary :include-macros true]
              [goog.events :as events]
              [goog.history.EventType :as EventType]
              [clojure.string :refer [capitalize]])
    (:import goog.History))

(def name-chars (map char (concat ;(range 48 57)
                                  ;(range 65 90)
                                  (range 97 122))))

(defn rand-char
  []
  (nth name-chars (rand-int (count name-chars))))

(defn rand-string
  [n]
  (apply str (repeatedly n rand-char)))

(defn rand-sentence
  [n]
  (let [words (repeatedly n #(rand-string (inc (rand-int 13))))]
  (capitalize (apply str (concat (interpose " " words) ["."])))))

(defn rand-paragraph
  [n]
  (apply str (interpose "  " (repeatedly (+ 5 (rand-int 12)) #(rand-sentence (+ 5 (rand-int 15)))))))

;(rand-paragraph (+ 1 (rand-int 3)))

(defn header []
  [:div
   [:h3 "(center :max-width 800)"]
   [:div.header

    [:h3 "(column :ratio 1/3)"]
    [:div.thirds
     [:div "a"]
     [:div "b"]
     [:div "c"]]

    [:h3 "(column :ratio 1/5)"]
    [:div.fifths
     [:div "1"]
     [:div "2"]
     [:div "3"]
     [:div "4"]
     [:div "5"]]

    [:h3 "(column :ratio 1/5 :offset 3/5)"]
    [:div.offset-fifths
     [:div "over here!"]]

    [:h3 "(column :ratio 1/3 :cycle 3)"]
    [:div.cycling
     (repeatedly 12 (fn [] [:div (rand-string 8)]))]

    [:h3 "(column :ratio 1/2)"]
    [:div.nested
     [:div
      (repeat 2
              [:div.inside
               [:h4 "(column :ratio 1/3 :gutter 10)"]
               (repeatedly 3 (fn [] [:div.a (rand-string 5)]))])]

     [:div
      (repeat 2
              [:div.inside
               [:h4 "(column :ratio 1/5)"]
               (repeatedly 5 (fn [] [:div.b (rand-string 5)]))])]]

    [:h3 "(span 1/4)"]
    [:div.spanning
     (repeatedly 8 (fn [] [:div (rand-string 5)]))]

    [:h3 "(stack :pad 10 :align :center)"]

    [:div.stacked
     (repeat 4 [:div (rand-string 10)])]

    [:h3 "(align :horizontal)"]
    [:div.aligned "aligned!"]
    ]])

(defn home-page []
  [:div [:h2 "Welcome to greenhouse"]
   [:div [:a {:href "#/about"} "go to about page"]]
   [header]])

(defn about-page []
  [:div [:h2 "About greenhouse"]
   [:div [:a {:href "#/"} "go to the home page"]]])

(defn current-page []
  [:div [(session/get :current-page)]])

;; -------------------------
;; Routes
(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
  (session/put! :current-page #'home-page))

(secretary/defroute "/about" []
  (session/put! :current-page #'about-page))

;; -------------------------
;; History
;; must be called after routes have been defined
(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
     EventType/NAVIGATE
     (fn [event]
       (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

;; -------------------------
;; Initialize app
(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (hook-browser-navigation!)
  (mount-root))
