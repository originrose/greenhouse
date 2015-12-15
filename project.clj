(defproject thinktopic/greenhouse "0.1.1"
  :description "A ratio grid library for garden css in Clojure."
  :url "http://github.com/thinktopic/garden"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [garden "1.3.0"]]

  :plugins [[lein-asset-minifier "0.2.2"]
            [lein-garden "0.2.6"]]

  :garden {:builds [{:id "screen"
                     :source-paths ["src/clj/greenhouse"]
                     :stylesheet greenhouse.demo.core/screen
                     :compiler {:output-to "resources/public/css/screen.css"
                                :pretty-print? true}}]}
  :ring {:handler greenhouse.handler/app
         :uberwar-name "greenhouse.war"}

  :min-lein-version "2.5.0"
  :uberjar-name "greenhouse.jar"

  :main greenhouse.server

  :clean-targets ^{:protect false} [:target-path
                                    [:cljsbuild :builds :app :compiler :output-dir]
                                    [:cljsbuild :builds :app :compiler :output-to]]

  :minify-assets
  {:assets
    {"resources/public/css/site.min.css" "resources/public/css/site.css"}}

  :cljsbuild {:builds {:app {:source-paths ["env/dev/cljs"]
                             :compiler {:output-to     "resources/public/js/app.js"
                                        :output-dir    "resources/public/js/out"
                                        :asset-path   "js/out"
                                        :optimizations :none
                                        :pretty-print  true}}}}

  :profiles {:dev {:repl-options {:init-ns greenhouse.repl
                                  :nrepl-middleware []}
                   :dependencies [[ring-server "0.4.0"]
                                  [ring "1.4.0"]
                                  [ring/ring-defaults "0.1.5"]
                                  [prone "0.8.2"]
                                  [compojure "1.4.0"]
                                  [hiccup "1.0.5"]
                                  [org.clojure/clojurescript "0.0-3308" :scope "provided"]
                                  [reagent "0.5.0"]
                                  [reagent-utils "0.1.4"]
                                  ;[cljsjs/react "0.13.3-1"]
                                  [secretary "1.2.3"]
                                  [ring/ring-mock "0.2.0"]
                                  [ring/ring-devel "1.4.0"]
                                  [lein-figwheel "0.3.7"]
                                  [org.clojure/tools.nrepl "0.2.10"]]
                   :source-paths ["env/dev/clj"]
                   :plugins [[lein-figwheel "0.3.7"]
                             [lein-cljsbuild "1.0.6"]]
                   :figwheel {:http-server-root "public"
                              :server-port 3449
                              :nrepl-port 7002
                              :css-dirs ["resources/public/css"]
                              :ring-handler greenhouse.handler/app}
                   :cljsbuild {:builds {:app {:source-paths ["env/dev/cljs"]
                                              :compiler {:main "greenhouse.dev"
                                                         :source-map true}}}}}})
