(ns rss.db
  (:import com.mchange.v2.c3p0.ComboPooledDataSource)
  (:require [clojure.java.jdbc :as sql]))

(def db-config
  {:classname "org.h2.Driver"
   :subprotocol "h2"
   :subname "~/.rss"
   :user ""
   :password ""})

(defn pool [config]
  (let [cpds (doto (ComboPooledDataSource.)
               (.setDriverClass (:classname config))
               (.setJdbcUrl (str "jdbc:" (:subprotocol config) ":" (:subname config)))
               (.setUser (:user config))
               (.setPassword (:password config))
               (.setMaxPoolSize 6)
               (.setMinPoolSize 1)
               (.setInitialPoolSize 1))]
    {:datasource cpds}))

(def pooled-db (delay (pool db-config)))

(defn db-connection [] @pooled-db)

(defmacro defdb [fname [& args] & body]
  `(defn ~fname [~@args]
     (sql/with-connection (db-connection)
       ~@body)))

(defdb table-exists? [table]
  (= '(1)
     (vals
      (sql/with-query-results results
        ["select count(*) from information_schema.tables where table_name = ?"
         (clojure.string/upper-case table)]
        (first results)))))

(defdb find-by [what by value]
  (sql/with-query-results results
    [(str "select * from " (name what) " where " (name by) " = ?") value]
    (vec results)))

(defn find-one [what id]
  (first (find-by what :id id)))

(defdb delete-by [what by value]
  (sql/transaction
   (sql/delete-rows what [(str (name by) " = ?") value])))

(defdb find-all [what]
  (sql/with-query-results results [(str "select * from " (name what))]
    (vec results)))

(defdb insert-in [table values]
  (first (vals
          (sql/transaction
           (sql/insert-values table (keys values) (vals values))))))

(defdb update-values [table where what values]
  (sql/transaction
   (sql/update-values table [(str (name where) " = ?") what] values)))

(defdb create-db [connection]
  (sql/create-table :feeds
                    [:id "bigint" "auto_increment" "primary key"]
                    [:url :varchar]
                    [:title :varchar]
                    [:description :varchar])
  (sql/create-table :entries
                    [:id "bigint" "auto_increment" "primary key"]
                    [:title :varchar]
                    [:description :varchar]
                    [:url :varchar]
                    [:read :boolean]
                    [:published :timestamp]
                    [:feed_id :bigint]))
