(ns flyingmachine.boot-datomic.core
  (:require [datomic.api :as d]
            [io.rkn.conformity :as c]
            [com.flyingmachine.vern :as v]
            [clojure.java.io :as io]))

(def default-schema   (delay (c/read-resource "db/schema.edn")))
(def default-fixtures (delay (let [f "db/fixtures.edn"]
                               (and (io/resource f) (c/read-resource f)))))

(defn load-fixtures
  [fixture-data]
  (let [entities (atom [])]
    (v/do-named (fn [processed group-name entity]
                  (swap! entities #(conj % (:data entity)))
                  (get-in entity [:data :db/id]))
                fixture-data)
    @entities))

(defn default-norm-map
  "Loads norm map from default sources"
  []
  (merge @default-schema
         (if @default-fixtures
           {:stack/fixtures {:txes [(load-fixtures @default-fixtures)]}})))

(defn conform
  "convenience method to conform both schema and fixtures from defaualt location"
  ([conn]
   (conform conn (default-norm-map)))
  ([conn & args]
   (apply c/ensure-conforms conn args)))

(defn attributes
  "list all installed attributes"
  [conn]
  (sort (d/q '[:find [?ident ...]
               :where
               [?e :db/ident ?ident]
               [_ :db.install/attribute ?e]]
             (d/db conn))))
