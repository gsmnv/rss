(ns rss.entry-controller
  (:use ring.util.response
        rss.db
        rss.entry-model))

(defn show [id]
  (response {:entry (find-one :entries id)}))

(defn update [id entry]
  (update-values :entries :id id entry)
  (response {}))
