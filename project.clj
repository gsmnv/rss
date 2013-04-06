(defproject rss "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [compojure "1.1.5"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [ring/ring-json "0.2.0"]
                 [c3p0/c3p0 "0.9.1.2"]
                 [org.clojure/java.jdbc "0.2.3"]
                 [org.clojars.scsibug/feedparser-clj "0.4.0"]
                 [com.h2database/h2 "1.3.168"]
                 [cheshire "5.0.2"]]
  :plugins [[lein-ring "0.8.3"]]
  :ring {:handler rss.handler/app
         :port 3001}
  :profiles
  {:dev {:dependencies [[ring-mock "0.1.3"]]}})
