(ns com.flyingmachine.datomic-booties.tasks
  {:boot/export-tasks true}
  (:require
   [boot.core    :as c :refer [deftask with-pre-wrap]]
   [boot.util    :as util]
   [datomic.api  :as d]
   [com.flyingmachine.datomic-booties.core :as bd]))

(defn- sym-with-env
  [sym ns]
  `[~sym (or ~sym (c/get-env ~(keyword (name ns) (name sym))))])

(defn- syms-with-env
  [ns syms]
  (vec (mapcat #(sym-with-env % ns) syms)))

(defmacro with-env
  "Create a let binding that looks up vars in boot's env if the var
  isn't provided
  
  Ex: (with-env :melange.build [file] (task-stuff file)) will check
  whether `file` exists, and if it doesn't, tries to bind it
  to `(get-env :melange.build/file)`"
  [ns syms & body]
  `(let ~(syms-with-env ns syms)
     ~@body))

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
     (with-env :datomic-booties [~'uri]
       ~'(if-not uri
           (do (util/fail "The -u/--uri option is required!\n") (*usage*)))
       ~@body
       identity)))

(defmacro defdatatask
  [name desc & body]
  `(deftask ~name
     ~desc
     ~'[u uri       VAL str   "Datomic URI"
        s schema    SCH [str] "Paths to schema defs in resources"
        d data      DAT [str] "Paths to seed files in resources"
        t transform TRX sym   "Name of some function to transform each seed data record"]
     (with-env :datomic-booties [~'uri ~'schema ~'data ~'transform]
       ~'(if-not uri
           (do (util/fail "The -u/--uri option is required!\n") (*usage*)))
       ~@body
       identity)))

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
