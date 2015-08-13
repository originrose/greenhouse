(ns greenhouse.fx
  (:require
    [garden.core :refer [css]]
    [garden.stylesheet :refer [at-media]]
    [garden.units :as u :refer [px pt em percent]]
    [garden.color :as color :refer [hsl rgb]]))

(defn rounded
  [& {:keys [radius top-left top-right bottom-right bottom-left]
      :or {top-left 0 top-right 0 bottom-right 0 bottom-left 0}}]
  (println "rounded radius: " radius)
  (list
    (if (pos? radius)
      {:-webkit-border-radius (px radius)
       :-moz-border-radius (px radius)
       :border-radius (px radius)}
      {:-webkit-border-top-right-radius (px top-right)
       :-webkit-border-bottom-right-radius (px bottom-right)
       :-webkit-border-bottom-left-radius (px bottom-left)
       :-webkit-border-top-left-radius (px top-left)
       :-moz-border-radius-topright (px top-right)
       :-moz-border-radius-bottomright (px bottom-right)
       :-moz-border-radius-bottomleft (px bottom-left)
       :-moz-border-radius-topleft (px top-left)
       :border-top-right-radius (px top-right)
       :border-bottom-right-radius (px bottom-right)
       :border-bottom-left-radius (px bottom-left)
       :border-top-left-radius (px top-left)
       ;:.background-clip(padding-box)
       })))
