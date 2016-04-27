(set-env!
 :source-paths #{"src/frontend" "src/backend"}
 :resource-paths #{"resources"}
 :dependencies '[[adzerk/boot-cljs      "1.7.228-1"       :scope "test"]
                 [adzerk/boot-cljs-repl "0.1.10-SNAPSHOT" :scope "test"]
                 [org.clojure/clojurescript "1.7.228"]
                 [adzerk/boot-reload    "0.4.4"           :scope "test"]
                 
                 ;; server
                 [environ                             "1.0.2"]
                 [boot-environ                        "1.0.2"]
                 [ring/ring-core                      "1.4.0"]
                 [ring/ring-defaults                  "0.1.4"]
                 [ring-middleware-format              "0.7.0"]
                 [com.stuartsierra/component          "0.3.1"]
                 [org.danielsz/system                 "0.2.0" :scope "test"]
                 [liberator                           "0.14.1"]
                 [http-kit                            "2.1.18"]
                 [com.datomic/datomic-free            "0.9.5344"]
                 [com.flyingmachine/liberator-unbound "0.1.1"]
                 [com.flyingmachine/datomic-junk      "0.2.3"]
                 [com.flyingmachine/vern              "0.1.0-SNAPSHOT"]
                 [compojure                           "1.5.0"]
                 [io.rkn/conformity                   "0.4.0"]
                 [buddy                               "0.13.0"]
                 [io.clojure/liberator-transit        "0.3.0"]
                 [medley                              "0.7.1"]
                 [clj-time                            "0.11.0"]

                 ;; client
                 [reagent                     "0.6.0-alpha" :exclusions [cljsjs/react]]
                 [cljsjs/react-with-addons    "0.13.3-0"]
                 [re-frame                    "0.7.0"]
                 [mathias/boot-sassc          "0.1.5" :scope "test"]
                 [cljs-ajax                   "0.5.4"]
                 [com.andrewmcveigh/cljs-time "0.4.0"]
                 [secretary                   "1.2.3"]])

(load-data-readers!)

(require
 '[system.boot                    :as sb]
 '[environ.core                   :as env]
 '[environ.boot                   :refer [environ]]
 '[datomic.api                    :as d]
 '[com.flyingmachine.datomic-junk :as dj]
 '[mathias.boot-sassc             :refer [sass]]
 '[stack.db.tasks                 :as dbt])

;; datomic
(defn db-uri [] (env/env :db-uri))
(defn connect [] (d/connect (db-uri)))

(deftask development
  "Set dev-specific env vars"
  []
  (environ :env {:http-server-port "3000"
                 :db-uri           "datomic:free://localhost:4334/stack"}))

(deftask migrate-db []
  (with-pre-wrap fileset
    (dbt/conform (connect))
    fileset))

(deftask create-db []
  (with-pre-wrap fileset
    (d/create-database (db-uri))
    fileset))

(deftask delete-db []
  (with-pre-wrap fileset
    (d/delete-database (db-uri))
    fileset))

(deftask recreate-db []
  (comp (delete-db) (create-db) (migrate-db)))

(deftask bootstrap []
  (comp (create-db)
        (migrate-db)))


(deftask run
  "Start hot-loading application"
  []
  (comp
   (watch)
   (repl :server true)))

(deftask dev
  "Simple alias to run application in development mode"
  []
  (set-env! :target-path "target/dev")
  (comp (development)
        (run)))
