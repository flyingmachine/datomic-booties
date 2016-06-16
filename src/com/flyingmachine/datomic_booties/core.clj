(ns com.flyingmachine.datomic-booties.core
  (:require [datomic.api :as d]
            [io.rkn.conformity :as c]
            [growmonster.core :as g]
            [clojure.java.io :as io]))

(def default-schema (delay (c/read-resource "db/schema.edn")))
(defn default-seed
  []
  (let [f "db/seed.edn"]
    (when-let [seed (and (io/resource f) (c/read-resource f))]
      {:datomic-booties/seed {:txes [(g/inflatev seed)]}})))

(defn default-norm-map
  "Loads norm map from default sources"
  []
  (merge @default-schema
         (default-seed)))

(defn seeds
  [paths]
  {:datomic-booties/seed
   {:txes [(->> paths
                (mapcat (fn [path]
                          (if (io/resource path)
                            (g/inflatev (c/read-resource path))
                            (throw (ex-info (str "Could not find seed file at path: " path)
                                            {:path path})))))
                (into []))]}})

(defn norm-map
  "Used to specify multiple schema and seed resource paths. Falls back
  to defaults."
  [schema-paths seed-paths]
  (merge
   (if (empty? schema-paths)
     @default-schema
     (apply merge
            (map (fn [path]
                   (if (io/resource path)
                     (c/read-resource path)
                     (throw (ex-info (str "Could not find schema file at path: " path)
                                     {:path path}))))
                 schema-paths)))
   (if (empty? seed-paths)
     (default-seed)
     (seeds seed-paths))))

(defn conform
  "convenience method to conform both schema and seed from default location"
  ([conn]
   (conform conn (default-norm-map)))
  ([conn & args]
   (apply c/ensure-conforms conn args)))

(defn attributes
  "list all installed attributes - useful for debugging"
  [conn]
  (sort (d/q '[:find [?ident ...]
               :where
               [?e :db/ident ?ident]
               [_ :db.install/attribute ?e]]
             (d/db conn))))
