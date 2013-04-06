(ns rss.entry-model
  (:use rss.db)
  (:require [clojure.java.jdbc :as sql]))

(defn create-entry [entry feed-id]
  (insert-in :entries {:title (:title entry)
                       :description (get-in entry [:description :value])
                       :url (:uri entry)
                       :feed_id feed-id
                       :published (:published-date entry)}))
