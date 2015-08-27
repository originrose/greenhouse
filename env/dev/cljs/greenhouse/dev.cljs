(ns ^:figwheel-no-load greenhouse.dev
  (:require [greenhouse.demo :as demo]
            [figwheel.client :as figwheel :include-macros true]))

(enable-console-print!)

(figwheel/watch-and-reload
  :websocket-url "ws://localhost:3449/figwheel-ws"
  :jsload-callback demo/mount-root)

(demo/init!)
