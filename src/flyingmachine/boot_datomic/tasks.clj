(ns flyingmachine.boot-datomic.tasks
  {:boot/export-tasks true}
  (:require
    [boot.core    :as core :refer [deftask with-pre-wrap]]
    [boot.util    :as util]
    [datomic.api  :as d]
    [flyingmachine.boot-datomic.core :as bd]))

(defmacro defdbtask
  [name & body]
  `(deftask ~name
     ~'[u uri VAL str "Datomic URI"]
     (with-pre-wrap fileset#
       ~'(if-not uri
           (do (util/fail "The -u/--uri option is required!") (*usage*)))
       ~@body
       fileset#)))

(defdbtask migrate-db
  (bd/conform (d/connect uri)))

(defdbtask create-db
  (d/create-database uri))

(defdbtask delete-db
  (d/delete-database uri))

(defdbtask bootstrap-db
  (comp (create-db  :uri uri)
        (migrate-db :uri uri)))

(defdbtask recreate-db
  (comp (delete-db    :uri uri)
        (bootstrap-db :uri uri)))
