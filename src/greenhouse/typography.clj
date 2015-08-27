 (ns greenhouse.typography
  (:require
    [garden.core :refer [css]]
    [garden.stylesheet :refer [at-media]]
    [garden.units :as u :refer [px pt em percent]]
    [garden.color :as color :refer [hsl rgb]]))

(def PHI 1.618033989)

(defn golden-line-height
  "Given a font size and content width use the golden ratio to
  compute the ideal line height."
  [& {:keys [font-size width]}]
  (let [width-ratio (/ width (Math/pow (* font-size PHI) 2))
        height-factor (- PHI (* (/ 1 (* 2 PHI))
                                (- 1 width-ratio)))]
    {:line-height (px (Math/round (* font-size height-factor)))}))

(defn golden-ratio
  [& {:keys [font-size width]
      :or {font-size 12 width 800}}]
  (list {:font-size (px font-size)
         :width (px width)}
        (golden-line-height :font-size font-size :width width)))

