(ns rss.feed-controller
  (:use ring.util.response
        rss.db
        rss.feed-model))

(defn index []
  (let [feeds (feeds-with-entries)]
    (response {:feeds feeds
               :entries (flatten (map #(find-all-entries (:id %)) feeds))})))

(defn show [id]
  (let [entries (find-all-entries id)]
    (response {:feed (feed-with-entries id) :entries entries})))

(defn delete [id]
  (destroy-feed id)
  (response {}))

(defn updates [id]
  (response (get-updates id)))

(defn create [params]
  (let [feed-id (create-feed-from-url (:url params))
        entries (find-all-entries feed-id)]
    (response {:feed (feed-with-entries feed-id) :entries entries})))
