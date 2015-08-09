(ns styles.core
  (:require
    [garden.def :refer [defstylesheet defstyles]]
    [garden.core :refer [css]]
    [garden.stylesheet :refer [at-media]]
    [garden.units :as u :refer [px pt em percent]]
    [garden.color :as color :refer [hsl rgba]]
    [styles.grid :refer [column span clearfix center stack align]]))

(defstyles screen
  [[:body
    {:font-family "sans-serif"
     :font-size (px 16)
     :line-height 1.5}]

   [:h3
    (clearfix)
    {:clear :both
     :padding-top (px 35)}]

   [:div.header
    ;{:border "1px solid grey"
    ;:background-color (rgba 100 100 100 0.1)}
    (center :max-width 800)]

   [:div.thirds
    [:div
     (column :ratio 1/3)
     {:background-color (rgba 250 200 200 0.5)
      :margin-bottom (px 5)}]]

   [:div.fifths
    [:div
     (column :ratio 1/5)
     {:background-color (rgba 200 250 200 0.5)
      :margin-bottom (px 5)}]]

   [:div.offset-fifths
    [:div
     (column :ratio 1/5 :offset 3/5)
     {:background-color (rgba 200 200 250 0.5)
      :margin-bottom (px 5)}]]

   [:div.cycling
    [:div
     (column :ratio 1/3 :cycle 3)
     {:background-color (rgba 250 200 250 0.5)
      :margin-bottom (px 5)}]]

   [:div.nested
    [:div.inside
     (column :ratio 1/2)
     {:background-color (rgba 200 200 150 0.5)}
     [:div.a
      (column :ratio 1/3 :gutter 10)
      {:background-color (rgba 150 200 250 0.5)}]
     [:div.b
      (column :ratio 1/5)
      {:background-color (rgba 250 150 250 0.5)}]]]

   [:div.spanning
    [:div
     (span :ratio 1/4)
     {:background-color (rgba 200 250 250 0.5)}]]

   [:div.stacked
    [:div
     (stack :pad 10 :align :center)
     {:background-color (rgba 250 250 200 0.5)}]]

   [:div.aligned
    (align :direction :horizontal)
    {:background-color (rgba 250 180 180 0.5)}]

  ])
