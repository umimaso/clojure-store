(ns clojure-store.routes.order
  (:require
   [clojure-store.layout :as layout]
   [clojure-store.db.core :as db]
   [clojure.java.io :as io]
   [clojure-store.middleware :as middleware]
   [ring.util.response]
   [ring.util.http-response :as response]))

(defn order-form [request]
  (layout/render
   request
   "order.html"
   (merge
    {:option-types (db/get-tshirt-option-types)}
    {:options (db/get-tshirt-options)})))

(defn new-order [request]
  (layout/render
   request
   "order-confirmation.html"
   (response/found "/order/confirmation")))

(defn order-routes []
  [""
   {:middleware [middleware/wrap-csrf
                 middleware/wrap-formats]}
   ["/order" {:get order-form
              :post new-order}]])
