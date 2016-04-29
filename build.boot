(set-env!
 :source-paths   #{"src"}
 :resource-paths #{}
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
  '[flyingmachine.boot-datomic.tasks :refer :all]
  '[flyingmachine.boot-datomic.core  :as c]
  '[datomic.api :as d])

;; This is necessary so that datomic tagged literals will load correctly
(load-data-readers!)

(def +version+ "0.1.0")
(bootlaces! +version+)

(task-options!
 pom  {:project     'flyingmachine/boot-datomic
       :version     +version+
       :description "Opions on basic boot tasks like migrating and adding fixtures"
       :url         "https://github.com/flyingmachine/boot-datomic"
       :scm         {:url "https://github.com/flyingmachine/boot-datomic"}
       :license     {"MIT" "https://opensource.org/licenses/MIT"} })

(deftask test-env []
  (set-env! :source-paths #{"test"})
  (set-env! :resource-paths #(conj % "dev-resources"))
  identity)

(deftask dev-env
  []
  (set-env! :resource-paths #(conj % "dev-resources"))
  identity)

(deftask dev
  []
  (comp
   (dev-env)
   (watch)
   (repl :server true)))

(def uri "datomic:free://localhost:4334/boot-datomic-dev")
