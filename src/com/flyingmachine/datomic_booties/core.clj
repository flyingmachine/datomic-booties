(ns com.flyingmachine.datomic-booties.core
  (:require [datomic.api :as d]
            [io.rkn.conformity :as c]
            [growmonster.core :as g]
            [clojure.java.io :as io]))

(defn seeds
  [paths seed-transform]
  {:datomic-booties/seed
   {:txes [(->> paths
                (mapcat (fn [path]
                          (if (io/resource path)
                            (g/inflatev (c/read-resource path) (or seed-transform identity))
                            (throw (ex-info (str "Could not find seed file at path: " path)
                                            {:path path})))))
                (into []))]}})

(defn schemas
  "Used to specify multiple schema and seed resource paths. Falls back
  to defaults."
  [schema-paths]
  (apply merge
         (map (fn [path]
                (if (io/resource path)
                  (c/read-resource path)
                  (throw (ex-info (str "Could not find schema file at path: " path)
                                  {:path path}))))
              schema-paths)))

(defn conform
  "convenience method to conform both schema and seed"
  ([conn schema-paths seed-paths seed-transform]
   (when (not-empty schema-paths)
     (c/ensure-conforms conn (schemas schema-paths)))
   (when (not-empty seed-paths)
     (c/ensure-conforms conn (seeds seed-paths seed-transform)))))

(defn attributes
  "list all installed attributes - useful for debugging"
  [conn]
  (sort (d/q '[:find [?ident ...]
               :where
               [?e :db/ident ?ident]
               [_ :db.install/attribute ?e]]
             (d/db conn))))
