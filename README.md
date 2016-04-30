# datomic-booties

Dependency info:

```clojure
[com.flyingmachine.datomic-booties "0.1.0"]
```

This library makes it easy to stand up a Datomic database with
Boot. It provides the following tasks:

* `create-db`
* `delete-db`
* `migrate-db` runs migrations as described below
* `bootstrap-db` combines `create-db` and `migate-db`
* `recreate-db` combines `delete-db` and `bootstrap-db`

## Migrations

datomic-booties looks for a `db/schema.edn` resource to define
database attributes; see the
[conformity README](https://github.com/rkneufeld/conformity) for
details on how to structure `db/schema.edn`. Example schema:

```clojure
{:booties/initial-schema
 {:txes [[{:db/ident :user/username
           :db/id #db/id [:db.part/db]
           :db/valueType :db.type/string
           :db/cardinality :db.cardinality/one
           :db/index true
           :db/fulltext true
           :db.install/_attribute :db.part/db}

          ;; posts
          {:db/ident :post/content
           :db/id #db/id[:db.part/db]
           :db/valueType :db.type/string
           :db/cardinality :db.cardinality/one
           :db/fulltext true
           :db/doc "Post content"
           :db.install/_attribute :db.part/db}

          ;; content
          {:db/ident :content/author
           :db/id #db/id[:db.part/db]
           :db/valueType :db.type/ref
           :db/cardinality :db.cardinality/one
           :db/doc "General author attribute"
           :db.install/_attribute :db.part/db}]]}}
```

It looks in `db/seed.edn` for seed data. See the
[growmonster README](https://github.com/flyingmachine/growmonster) for
details on seed data format.

Example:

```clojure
[:users
 [:billy
  {:db/id #db/id [:db.part/user]
   :user/username "billy"}]

 :posts
 [{:db/id #db/id [:db.part/user]
   :post/content "post content"
   :content/author [:users :billy :db/id]}]]
```
