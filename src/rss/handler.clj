(ns rss.handler
  (:use rss.db)
  (:use rss.feed-model)
  (:use rss.entry-model)
  (:use compojure.core)
  (:use cheshire.core)
  (:use ring.util.response)
  (:require [compojure.handler :as handler]
            [ring.middleware.json :as middleware]
            [clojure.contrib.seq-utils :as sec-utils]
            [rss.feed-controller :as feed]
            [rss.entry-controller :as entry]
            [feedparser-clj.core :as feedparser]
            [clojure.java.jdbc :as sql]
            [compojure.route :as route]))

(defroutes feeds-routes
  (GET "/" [] (feed/index))
  (POST "/" [feed] (feed/create feed))
  (DELETE "/:id" [id] (feed/delete id))
  (GET "/:id" [id] (feed/show id))
  (GET "/:id/updates" [id] (feed/updates id)))

(defroutes entries-routes
  (GET "/:id" [id] (entry/show id))
  (PUT "/:id" [entry id] (entry/update id entry)))

(defroutes app-routes
  (context "/feeds" [] feeds-routes)
  (context "/entries" [] entries-routes)
  (GET "/" [] (file-response "index.html" {:root "resources/public"}))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (-> (handler/site app-routes)
      (middleware/wrap-json-body)
      (middleware/wrap-json-params)
      (middleware/wrap-json-response)))

(when-not (table-exists? "feeds")
  (do (create-db (db-connection))))
