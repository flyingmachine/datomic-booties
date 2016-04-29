(ns flyingmachine.boot-datomic.tasks
  {:boot/export-tasks true}
  (:require
    [boot.core    :as core :refer [deftask with-pre-wrap]]
    [boot.util    :as util]
    [datomic.api  :as d]
    [flyingmachine.boot-datomic.core :as bd]))

(deftask migrate-db
  [u uri VAL str "Datomic URI"]
  (with-pre-wrap fileset
    (bd/conform (d/connect uri))
    fileset))

(deftask create-db
  [u uri VAL str "Datomic URI"]
  (with-pre-wrap fileset
    (d/create-database uri)
    fileset))

(deftask delete-db
  [u uri VAL str "Datomic URI"]
  (with-pre-wrap fileset
    (d/delete-database uri)
    fileset))

(deftask bootstrap-db
  [u uri VAL str "Datomic URI"]
  (comp (create-db  :uri uri)
        (migrate-db :uri uri)))

(deftask recreate-db
  [u uri VAL str "Datomic URI"]
  (comp (delete-db    :uri uri)
        (bootstrap-db :uri uri)))
