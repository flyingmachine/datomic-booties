(set-env!
 :source-paths   #{"src" "test"}
 :resource-paths #{"dev-resources"}
 :target-path    "target/build"
 :dependencies   '[[org.clojure/clojure      "1.7.0"    :scope "provided"]
                   [boot/core                "2.5.5"    :scope "provided"]
                   [adzerk/bootlaces         "0.1.13"   :scope "test"]
                   [adzerk/boot-test         "1.1.1"    :scope "test"]
                   [com.datomic/datomic-free "0.9.5344" :scope "test"]
                   [growmonster              "0.1.0"]
                   [io.rkn/conformity        "0.4.0"]])

(require
  '[adzerk.bootlaces                 :refer :all]
  '[adzerk.boot-test                 :refer :all]
  '[com.flyingmachine.datomic-booties.tasks :refer :all]
  '[com.flyingmachine.datomic-booties.core  :as c]
  '[datomic.api :as d])

;; This is necessary so that datomic tagged literals will load correctly
(load-data-readers!)

(def +version+ "0.1.1")
(bootlaces! +version+)

(task-options!
 pom  {:project     'com.flyingmachine/datomic-booties
       :version     +version+
       :description "Opinions on basic boot tasks like migrating and adding seed data"
       :url         "https://github.com/com.flyingmachine.datomic-booties"
       :scm         {:url "https://github.com/com.flyingmachine.datomic-booties"}
       :license     {"MIT" "https://opensource.org/licenses/MIT"} })

(deftask prebuild
  "Remove directories that shouldn't go into the final jar"
  []
  (set-env! :source-paths #(into #{} (remove #{"test"} %)))
  (set-env! :resource-paths #(into #{} (remove #{"dev-resources"} %)))
  identity)

(deftask dev
  []
  (comp (watch)
        (repl :server true)))

(def uri "datomic:free://localhost:4334/datomic-booties-dev")
