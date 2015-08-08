(ns styles.core
  (:require
    [garden.def :refer [defstylesheet defstyles]]
    [garden.core :refer [css]]
    [garden.stylesheet :refer [at-media]]
    [garden.units :as u :refer [px pt em percent]]
    [garden.color :as color :refer [hsl rgba]]
    [styles.grid :refer [column clearfix center]]))

(defstyles screen
  [[:body
    {:font-family "sans-serif"
     :font-size (px 16)
     :line-height 1.5}]

   [:h3
    {:clear :both
     ;:border-top "2px solid blue"
     :padding (px 3)}]

   [:div.header
    ;{:border "1px solid grey"
    ;:background-color (rgba 100 100 100 0.1)}
    (center :max-width 800)]

   [:div.thirds
    [:div
     {:background-color (rgba 250 200 200 0.5)
      :margin-bottom (px 5)}
     (column :ratio 1/3)]]

   [:div.fifths
    [:div
     {:background-color (rgba 200 250 200 0.5)
      :margin-bottom (px 5)}
     (column :ratio 1/5)]]

   [:div.offset-fifths
    [:div
     {:background-color (rgba 200 200 250 0.5)
      :margin-bottom (px 5)}
     (column :ratio 1/5 :offset 3/5)
     ]]

   [:div.cycling
    [:div
     (column :ratio 1/3 :cycle 3)
     {:background-color (rgba 250 200 250 0.5)
      :margin-bottom (px 5)}
     ]]
  ])
