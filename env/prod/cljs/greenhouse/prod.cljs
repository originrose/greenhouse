(ns greenhouse.prod
  (:require [greenhouse.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init!)
