(ns clojure-store.db.core
  (:require
   [next.jdbc.date-time]
   [next.jdbc.result-set]
   [conman.core :as conman]
   [mount.core :refer [defstate]]
   [clojure-store.config :refer [env]]))

(defstate ^:dynamic *db*
  :start (conman/connect! {:jdbc-url (env :database-url)})
  :stop (conman/disconnect! *db*))

(conman/bind-connection *db* "sql/queries.sql")
