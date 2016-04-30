(ns com.flyingmachine.datomic-booties.tasks
  {:boot/export-tasks true}
  (:require
    [boot.core    :as core :refer [deftask with-pre-wrap]]
    [boot.util    :as util]
    [datomic.api  :as d]
    [com.flyingmachine.datomic-booties.core :as bd]))

(defmacro defdbtask
  [name desc & body]
  `(deftask ~name
     ~desc
     ~'[u uri VAL str "Datomic URI"]
     ~'(if-not uri
         (do (util/fail "The -u/--uri option is required!") (*usage*)))
     ~@body
     identity))

(defdbtask migrate-db
  "Conform schema and fixtures"
  (bd/conform (d/connect uri)))

(defdbtask create-db
  "Create datomic db"
  (d/create-database uri))

(defdbtask delete-db
  "Delete datomic db"
  (d/delete-database uri))

(defdbtask bootstrap-db
  "Create and migrate db"
  (comp (create-db  :uri uri)
        (migrate-db :uri uri)))

(defdbtask recreate-db
  "Delete then bootstrap db"
  (comp (delete-db    :uri uri)
        (bootstrap-db :uri uri)))
