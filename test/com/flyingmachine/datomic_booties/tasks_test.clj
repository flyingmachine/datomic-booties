(ns com.flyingmachine.datomic-booties.tasks-test
  (:require [com.flyingmachine.datomic-booties.tasks :as tasks]
            [com.flyingmachine.datomic-booties.core :as core]
            [boot.core :refer [boot deftask]]
            [datomic.api :as d]
            [clojure.test :refer [deftest is use-fixtures]]))

(def uri "datomic:mem://datomic-booties-test")

(defn with-db [f]
  (boot (tasks/create-db :uri uri))
  (f)
  (println "killed!")
  (boot (tasks/delete-db :uri uri)))

(use-fixtures :each with-db)

(deftest migrate
  (boot (tasks/migrate-db :uri uri))
  (is
   (= (d/q '[:find (pull ?e [*])
             :where (or [?e :user/username]
                        [?e :post/content])]
           (d/db (d/connect uri)))
      [[{:db/id 17592186045422
         :user/username "billy"}]
       [{:db/id 17592186045423
         :post/content "post content"
         :content/author {:db/id 17592186045422}}]]))
  (boot (tasks/delete-db :uri uri)))

(deftest custom-schema-and-seed
  (boot (tasks/bootstrap-db :uri uri
                            :schema ["db/custom-schema.edn" "db/custom-schema-2.edn"]
                            :data ["db/custom-seed.edn"]))
  (let [attrs (into #{} (core/attributes (d/connect uri)))]
    (is (every? attrs [:custom/attr :custom/mixy-matchy]))
    (is (not-any? attrs [:user/username :post/content]))
    (is (= (d/q '[:find (pull ?e [*])
                  :where [?e :custom/attr]]
                (d/db (d/connect uri)))
           [[{:db/id 17592186045423
              :custom/attr "billy"}]]))))
