(ns greenhouse.grid
  (:require
    [garden.core :refer [css]]
    [garden.stylesheet :refer [at-media]]
    [garden.units :as u :refer [px pt em percent]]
    [garden.color :as color :refer [hsl rgb]]))

(def ^:dynamic *layout-direction* :left->right)
(def ^:dynamic *default-gutter* 3)
(def ^:dynamic *parent-first* false)
(def ^:dynamic *max-width* (px 1440))

(def ^:dynamic *settings*
  {:min-width (px 400)
   :max-width (px 1200)
   :min-font (px 12)
   :max-font (px 32)
   ;:body-font (:eb-garamond typo/font-families)
   ;:header-font (:eb-garamond typo/font-families)
   :header-font-weight 600
   :header-color "#111"
   :scale :golden-ratio
   :media :screen
   :breakpoints
   {:mobile {:screen true
             :min-width (px 320)}
    :tablet {:screen true
             :min-width (px 768)}
    :laptop {:screen true
             :min-width (px 1260)}
    :desktop {:screen true
              :min-width (px 1824)}
    :hd {:screen true
         :min-width (px 1824)}}})

;(defmacro with-settings
;  [settings & body]
;  (binding [*settings* (merge *settings* settings)]
;    ))

(defn media-rules
  [label]
  (get-in *settings* [:breakpoints label]))

(defn on
  "Set rules for a particular breakpoint, by name.

  (on :tablet
    [:div
      (column :ratio 1/2)])
  "
  [media rules]
  (at-media (media-rules media) rules))

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

(defn uncenter
  []
  {:max-width :none
   :margin-right 0
   :margin-left 0
   :padding-left 0
   :padding-right 0})

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
  ([n side margin]
   (cycle-props n side margin margin))

  ([n side margin-r margin-last]
   (if (zero? n)
     [:&:last-child {(opposite-margin side) (percent margin-last)}]

     [["&:nth-of-type(n)"
       {(opposite-margin side) (percent margin-r)
        :float side
        :clear :none}]

      [(str "&:nth-of-type(" n "n)")
       {(opposite-margin side) (percent margin-last)
        :float (opposite-side side)}]

      [(str "&:nth-of-type(" n "n+1)")
       {:clear :both}]])))

;(defn grid
;  []
;  (list
;    {:display :flex
;     :flex-wrap :wrap}))

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
    (list {
           ;:flex col-width

           :float side
           :clear :none
           :width (percent col-width)
           (margin side) (percent margin-l)
           (opposite-margin side) (percent margin-r)}
          cycles)))

(defn span
  [& {:keys [ratio width offset cycle uncycle gutter]
      :or {ratio 1 offset 0 cycle 0 uncycle 0}}]
  (let [side (direction->side *layout-direction*)
        span-width (if width
                width
                (* ratio 100))
        margin-r (if (neg? offset) (* (- offset) 100) 0)
        margin-l (if (pos? offset) (* offset 100) 0)
        cycles (if (pos? cycle)
                 [[(str "&:nth-of-type(" cycle "n)")
                   {:float (opposite-side side)}]
                  [(str "&:nth-of-type(" cycle "n+1)")
                   {:clear :both}]]
                 [])]
    (list {;:flex (if width
           ;        [[0 0 (px span-width)]]
           ;        span-width)
           :float side
           :clear :none
           :width (percent span-width)
           (margin side) (percent margin-l)
           (opposite-margin side) (percent margin-r)}
          cycles)))

(defn stack
  "Examples:
  (stack)
  (stack :pad 10 :align :center)
  "
  [& {:keys [pad align]
      :or {pad 0 align false}}]
  (let [side (direction->side *layout-direction*)]
    (list
    {:display :block
     :clear :both
     :float :none
     :width (percent 100)
     :margin-left :auto
     :margin-right :auto}
    (if (pos? pad)
      {:padding-left pad
       :padding-right pad}
      [])
    (if align
      {:text-align align})
    [[:&:first-child {(margin side) :auto}]
     [:&:last-child {(opposite-margin side) :auto}]])))

(defn unstack
  []
  (let [side (direction->side *layout-direction*)]
    (list
      {:text-align side
       :display :inline
       :clear :none
       :width :auto
       :margin-left 0
       :margin-right 0
       }
      [[:&:first-child {(margin side) 0}]
       [:&:last-child {(opposite-margin side) 0}]])))

(def alignments
  {:horizontal {:left (percent 50)
                :transform "translateX(-50%)"}
   :vertical {:top (percent 50)
              :transform "translateY(-50%)"}
   :none {:top :auto
          :left :auto
          :transform "translate(0, 0)"}
   :both {:top (percent 50)
          :left (percent 50)
          :transform "translate(-50%, -50%)"}})

(defn align
  [& {:keys [direction]
      :or {direction :both}}]
  (merge {:position :absolute
          :transform-style :preserve-3d}
         (get alignments direction)))

