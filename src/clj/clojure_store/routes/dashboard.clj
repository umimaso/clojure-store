(ns clojure-store.routes.dashboard
  (:require
   [clojure-store.layout :as layout]
   [clojure.java.io :as io]
   [clojure-store.middleware :as middleware]
   [ring.util.response]
   [ring.util.http-response :as response]))

(defn dashboard [request]
  (layout/render request "dashboard.html"))

(defn dashboard-routes []
  [""
   {:middleware [middleware/wrap-csrf
                 middleware/wrap-formats]}
   ["/" {:get dashboard}]
   ["/dashboard" {:get dashboard}]])
