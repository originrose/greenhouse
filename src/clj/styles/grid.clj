(ns styles.grid
  (:require
    [garden.core :refer [css]]
    [garden.stylesheet :refer [at-media]]
    [garden.units :as u :refer [px pt em percent]]
    [garden.color :as color :refer [hsl rgb]]))


; (css (at-media {:screen true} [:h1 {:font-weight "bold"}]))
;    =>   "@media screen{h1{font-weight:bold}}"

; (css (at-media {:min-width (px 768) :max-width (px 979)}
;          [:container {:width (px 960)}])
;    => "@media (max-width:979px) and (min-width:768px){container{width:960px}}"

(def ^:dynamic *layout-direction* :left->right)
(def ^:dynamic *default-gutter* 3)
(def ^:dynamic *parent-first* false)
(def ^:dynamic *max-width* (px 1440))

(def settings
  {:min-width (px 400)
   :max-width (px 1200)
   :min-font (px 12)
   :max-font (px 32)
   ;:body-font (:eb-garamond typo/font-families)
   ;:header-font (:eb-garamond typo/font-families)
   :header-font-weight 600
   :header-color "#111"
   :scale :golden-ratio
   :breakpoints {:mobile (px 480)
                 :tablet (px 960)
                 :laptop (px 1440)
                 :monitor (px 1920)}})

(def fonts {:font-size-base (em 1.5)
            :line-height-base (em 1.45)
            :ff-serif ["EB Garamond" "Serif"]
            :ff-sans ["Fira Sans" "sans-serif"]
            :ff-mono ["Source Code Pro" "monospace"]})

(defn clearfix
  []
  [[:&:before :&:after
   {:content "''"
    :display :table}]
  [:&:after
   {:clear :both}]])

(def direction->side
  {:left->right :left
   :right->left :right})

(def opposite-side
  {:left :right
   :right :left
   :top :bottom
   :bottom :top})

(defn center
  "Horizontal Centering Block Elements"
  [& {:keys [max-width pad]
      :or {max-width (px 1410) pad 0}}]
  (clearfix)
  {:width :auto
   :max-width max-width
   :float :none
   :display :block
   :margin-right :auto
   :margin-left :auto
   :padding-left pad
   :padding-right pad})

(defn margin
  [side]
  (keyword (str "margin-" (name side))))

(defn opposite-margin
  [side]
  (keyword (str "margin-" (name (opposite-side side)))))

(defn- column-width
  "Given a column ratio (with respect to its parent) and a gutter ratio
  (with respect to the gutter), return the percent size of each."
  [ratio gutter]
  (let [gutter-width (* 100 (/ gutter 100))
        gutter-portion (* ratio gutter-width)
        col-percent (* 100 ratio)
        col-width (+ (- col-percent gutter-width) gutter-portion)]
    [col-width gutter-width]))

(defn cycle-props
  "Returns the relevant properties to implementing column cycling."
  [cycle side margin-r margin-last]
  (if (zero? cycle)
    [:&:last-child {(opposite-margin side) (percent margin-last)}]

    [["&:nth-of-type(n)"
     {(opposite-margin side) (percent margin-r)
      :float side
      :clear :none}]

     [(str "&:nth-of-type(" cycle "n)")
     {(opposite-margin side) (percent margin-last)
      :float (opposite-side side)}]

     [(str "&:nth-of-type(" cycle "n+1)")
     {:clear :both}]]))

(defn column
  [& {:keys [ratio offset cycle uncycle gutter]
      :or {ratio 1 offset 0 cycle 0 uncycle 0}}]
  (let [side (direction->side *layout-direction*)
        gutter (or gutter *default-gutter*)
        [col-width gutter-width] (column-width ratio gutter)
        margin-l 0
        margin-r gutter-width
        [margin-l margin-r] (cond
                              (zero? offset) [margin-l margin-r]

                              (pos? offset)
                              [(+ gutter-width (first (column-width offset gutter-width)))
                                             margin-r]

                              :negative-offset [margin-l
                                                (+ (* gutter-width 2)
                                                   (column-width (- offset) gutter-width))])
        margin-last (if (neg? offset) margin-r 0)
        cycles (cycle-props cycle side margin-r margin-last)]
    (list {:float side
           :clear :none
           :width (percent col-width)
           (margin side) (percent margin-l)
           (opposite-margin side) (percent margin-r)}
          cycles)))

(defn span
  [& {:keys [ratio offset cycle uncycle gutter]
      :or {ratio 1 offset 0 cycle 0 uncycle 0}}]
  (let [side (direction->side *layout-direction*)
        width (* ratio 100)
        margin-r (if (neg? offset) (* (- offset) 100) 0)
        margin-l (if (pos? offset) (* offset 100) 0)
        cycles (if (pos? cycle)
                 [[(str "&:nth-of-type(" cycle "n)")
                   {:float (opposite-side side)}]
                  [(str "&:nth-of-type(" cycle "n+1)")
                   {:clear :both}]]
                 [])]
    (list {:float side
           :clear :none
           :width (percent width)
           (margin side) (percent margin-l)
           (opposite-margin side) (percent margin-r)}
          cycles)))
