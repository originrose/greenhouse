(ns greenhouse.fx
  (:require
    [garden.core :refer [css]]
    [garden.stylesheet :refer [at-media]]
    [garden.units :as u :refer [px pt em percent]]
    [garden.color :as color :refer [hsl rgb]]
    [greenhouse.grid :refer [direction->side opposite-side]]))

(defn rounded
  [& {:keys [radius top-left top-right bottom-right bottom-left]}]
  (list
    (if (pos? radius)
      {:-webkit-border-radius (px radius)
       :-moz-border-radius (px radius)
       :border-radius (px radius)}
      (merge {}
             (if top-right
               {:-webkit-border-top-right-radius (px top-right)
                :border-top-right-radius (px top-right)
                :-moz-border-radius-topright (px top-right)}
               {})

             (if bottom-right
               {:-webkit-border-bottom-right-radius (px bottom-right)
                :-moz-border-radius-bottomright (px bottom-right)
                :border-bottom-right-radius (px bottom-right)}
               {})

             (if bottom-left
               {:-webkit-border-bottom-left-radius (px bottom-left)
                :-moz-border-radius-bottomleft (px bottom-left)
                :border-bottom-left-radius (px bottom-left)}
               {})

             (if top-left
               {:-webkit-border-top-left-radius (px top-left)
                :-moz-border-radius-topleft (px top-left)
                :border-top-left-radius (px top-left)}
               {}))
       ;:.background-clip(padding-box)
       )))

(defn border-side
  [side]
  (keyword (str "border-" (name side))))

(def orthogonal
  {:right [:top :bottom]
   :left [:top :bottom]
   :bottom [:left :right]
   :top [:left :right]})

(defn arrow
  [& {:keys [direction size color]
      :or {direction :right size 10 color :black}}]
  (let [opposite (opposite-side direction)
        [a b] (orthogonal direction)]
    (list
      {:width 0
       :height 0
       (border-side opposite) [[(px size) :solid color]]
       (border-side a) [[(px size) :solid :transparent]]
       (border-side b) [[(px size) :solid :transparent]]})))

