; Defines routes for the application
(ns clojure-store.routes.home
  (:require
   [clojure-store.layout :as layout]
   [clojure.java.io :as io]
   [clojure-store.middleware :as middleware]
   [ring.util.response]
   [ring.util.http-response :as response]))

(defn dashboard [request]
  (layout/render request "dashboard.html"))

(defn home-routes []
  [""
   {:middleware [middleware/wrap-csrf
                 middleware/wrap-formats]}
   ["/" {:get dashboard}]])

