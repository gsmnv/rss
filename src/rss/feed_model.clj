(ns rss.feed-model
  (:use rss.db)
  (:use rss.entry-model)
  (:require [clojure.java.jdbc :as sql]
            [feedparser-clj.core :as feedparser]))

(defn filter-new-entries [old-entries new-entries]
  (filter (fn [x] (not-any? #(= (:uri x) (:url %)) old-entries)) new-entries))

(defn create-feed [feed url]
  (insert-in :feeds {:title (:title feed)
                     :description (:description feed)
                     :url url}))

(defn create-feed-from-url [url]
  (let [feed (feedparser/parse-feed url)
        feed-id (create-feed feed url)]
    (doseq [entry (:entries feed)]
      (create-entry entry feed-id))
    feed-id))

(defn destroy-feed [id]
  (delete-by :feeds :id id)
  (delete-by :entries :feed_id id))

(defn find-all-entries [id]
  (find-by :entries :feed_id id))

(defn get-updates [id]
  (map #(find-one :entries (create-entry % id))
       (filter-new-entries
        (find-all-entries id)
        (:entries (feedparser/parse-feed (:url (find-one :feeds id)))))))

(defn feed-with-entries [id]
  (let [entries (find-all-entries id)]
    (assoc (find-one :feeds id) :entry_ids
           (map :id entries))))

(defn feeds-with-entries []
  (map feed-with-entries (map :id (find-all :feeds))))
