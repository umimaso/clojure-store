(ns clojure-store.handlers
  (:require
   [clojure-store.db.core :as db]))

(defn get-stock []
  (db/get-stock))
