(ns com.flyingmachine.datomic-booties.tasks-test
  (:require [com.flyingmachine.datomic-booties.tasks :as tasks]
            [boot.core :refer [boot deftask]]
            [datomic.api :as d]
            [clojure.test :refer [deftest is]]))

(def uri "datomic:mem://datomic-booties-test")

(deftest migrate
  (boot (tasks/bootstrap-db :uri uri))
  (is
   (= (d/q '[:find (pull ?e [*])
             :where (or [?e :user/username]
                        [?e :post/content])]
           (d/db (d/connect uri)))
      [[{:db/id 17592186045422
         :user/username "billy"}]
       [{:db/id 17592186045423
         :post/content "post content"
         :content/author {:db/id 17592186045422}}]])))
