(ns clojure-store.routes.order
  (:require
   [clojure-store.layout :as layout]
   [clojure.java.io :as io]
   [clojure-store.middleware :as middleware]
   [ring.util.response]
   [ring.util.http-response :as response]))

(defn order [request]
  (layout/render request "order.html"))

(defn order-routes []
  [""
   {:middleware [middleware/wrap-csrf
                 middleware/wrap-formats]}
   ["/order" {:get order}]])
