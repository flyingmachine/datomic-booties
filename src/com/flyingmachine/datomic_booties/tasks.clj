(ns com.flyingmachine.datomic-booties.tasks
  {:boot/export-tasks true}
  (:require
   [boot.core    :as c :refer [deftask with-pre-wrap]]
   [boot.util    :as util]
   [datomic.api  :as d]
   [com.flyingmachine.datomic-booties.core :as bd]))

(defn sym->var
  [sym]
  (if (symbol? sym)
    (-> sym namespace symbol find-ns (ns-resolve sym))
    sym))

(defmacro defdbtask
  [name desc & body]
  `(deftask ~name
     ~desc
     ~'[u uri    VAL str   "Datomic URI"]
     ~'(if-not uri
         (do (util/fail "The -u/--uri option is required!\n") (*usage*)))
     ~@body
     identity))

(defmacro defdatatask
  [name desc & body]
  `(deftask ~name
     ~desc
     ~'[u uri       VAL str   "Datomic URI"
        s schema    SCH [str] "Paths to schema defs in resources"
        d data      DAT [str] "Paths to seed files in resources"
        t transform TRX sym   "Name of some function to transform each seed data record"]
     ~'(if-not uri
         (do (util/fail "The -u/--uri option is required!\n") (*usage*)))
     ~@body
     identity))

(defdatatask migrate-db
  "Conform schema and fixtures"
  (let [transform (sym->var transform)]
    (bd/conform (d/connect uri) (bd/norm-map schema data transform))))

(defdbtask create-db
  "Create datomic db"
  (d/create-database uri))

(defdbtask delete-db
  "Delete datomic db"
  (d/delete-database uri))

(defdatatask bootstrap-db
  "Create and migrate db"
  (comp (create-db  :uri uri)
        (migrate-db :uri uri
                    :schema schema
                    :data data
                    :transform transform)))

(defdatatask recreate-db
  "Delete then bootstrap db"
  (comp (delete-db    :uri uri)
        (bootstrap-db :uri uri
                      :schema schema
                      :data data
                      :transform transform)))
