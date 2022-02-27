(ns clojure-store.handlers
  (:require
   [clojure-store.db.core :as db]
   [clojure.tools.logging :as log]))

; Request methods for backend to use
(defn req [func]
  (get (func) :body))

(defn req-query [func params]
  (get (func {:parameters {:query (into {} params)}}) :body))

(defn req-body [func params]
  (get (func {:parameters {:body (into {} params)}}) :body))

; Stock endpoint methods
(defn get-stock [& req]
  {:status 200
   :body (db/get-stock)})

(defn update-stock-option [req]
  (let [option-id (get (get (get req :parameters) :body) :tshirt_option_id)]
    (let [stock-count (get (get (get req :parameters) :body) :stock_count)]
      (do
        (db/update-stock-for-option-id! {:option_id option-id, :stock_count stock-count})
        {:status 200
         :body (db/get-stock)}))))

(defn remove-stock-option [req]
  (let [option-id (get (get (get req :parameters) :body) :tshirt_option_id)]
    (do
      (db/remove-stock-limit-for-option-id! {:option_id option-id})
      {:status 200
       :body (db/get-stock)})
    {:status 400}))

; Option endpoint methods
(defn get-options [& req]
  {:status 200
   :body (db/get-options)})

(defn get-option-types [& req]
  {:status 200
   :body (db/get-option-types)})

; Order endpoint methods
(defn get-orders [& req]
  {:status 200
   :body (db/get-orders)})

(defn new-order [req]
  (let [params (get (get req :parameters) :body)]
    (let [new-order (db/create-order! params)]
      {:status 200
       :body new-order})))

(defn new-order-option [req]
  (let [params (get (get req :parameters) :body)]
    (db/create-order-option! params)
    {:status 200
     :body (db/get-orders)}))
