(ns flyingmachine.boot-datomic.tasks-test
  (:require [flyingmachine.boot-datomic.tasks :as tasks]
            [boot.core :refer [boot deftask]]
            [datomic.api :as d]
            [clojure.test :refer [deftest]]))

(def uri "datomic:mem://boot-datomic-test")

(deftest migrate
  (boot (tasks/bootstrap-db :uri uri))
  (= (d/q '[:find (pull ?e [*])
            :where (or [?e :user/username]
                       [?e :post/content])]
          (d/db (d/connect uri)))
     [[{:db/id 17592186045422
        :user/username "billy"}]
      [{:db/id 17592186045423
        :post/content "post content"
        :content/author {:db/id 17592186045422}}]]))
