(defproject stickies "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.229"]
                 [reagent "0.6.0"]
                 [re-frame "0.9.2"]
                 [cljs-ajax "0.6.0"]
                 [io.nervous/cljs-nodejs-externs "0.2.0"]
                 [org.clojure/tools.cli "0.3.3"]]

  :plugins [[lein-cljsbuild "1.1.4"]
            [lein-less "1.7.5"]
            [lein-kibit "0.1.3"]
            [lein-pdo "0.1.1"]
            [lein-shell "0.5.0"]
            [lein-npm "0.6.1"]]

  :min-lein-version "2.5.3"

  :source-paths ["src/clj"]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"
                                    "test/js"]

  :figwheel {:css-dirs ["resources/public/css"]}

  :less {:source-paths ["less"]
         :target-path  "resources/public/css"}

  :npm {:dependencies [[source-map-support "0.4.0"]
                       [body-parser "1.17.2"]
                       [cors "2.8.3"]
                       [express "4.15.3"]
                       [front-matter "2.1.2"]
                       [js-yaml "3.8.4"]
                       [lodash.throttle "4.1.1"]
                       [mz "2.6.0"]]}

  :profiles
  {:dev
   {:dependencies [[binaryage/devtools "0.8.2"]]

    :plugins      [[lein-figwheel "0.5.9"]
                   [lein-doo "0.1.7"]]}}

  :aliases
  {"dev" ["do" "clean"
            ["pdo" ["figwheel" "dev"]
                   ["less" "auto"]
                   ["cljsbuild" "auto" "backend"]
                   ["shell" "nodemon" "--watch" "build/main.js" "build/main.js"]]]
   "build" ["do" "clean"
              ["cljsbuild" "once" "min"]
              ["cljsbuild" "once" "backend"]
              ["less" "once"]]}

  :cljsbuild
  {:builds
   [{:id           "dev"
     :source-paths ["src/cljs"]
     :figwheel     {:on-jsload "stickies.core/mount-root"}
     :compiler     {:main                 stickies.core
                    :output-to            "resources/public/js/compiled/app.js"
                    :output-dir           "resources/public/js/compiled/out"
                    :asset-path           "js/compiled/out"
                    :source-map-timestamp true
                    :preloads             [devtools.preload]
                    :external-config      {:devtools/config {:features-to-install :all}}}}


    {:id           "min"
     :source-paths ["src/cljs"]
     :compiler     {:main            stickies.core
                    :output-to       "resources/public/js/compiled/app.js"
                    :optimizations   :advanced
                    :closure-defines {goog.DEBUG false}
                    :pretty-print    false}}

    {:id           "test"
     :source-paths ["src/cljs" "test/cljs"]
     :compiler     {:main          stickies.runner
                    :output-to     "resources/public/js/compiled/test.js"
                    :output-dir    "resources/public/js/compiled/test/out"
                    :optimizations :none}}

    {:id "backend"
     :source-paths ["src/backend"]
     :compiler {:output-to "build/main.js"
                :output-dir "build/js"
                :optimizations :advanced
                :target :nodejs
                :source-map "build/main.js.map"}}]})
