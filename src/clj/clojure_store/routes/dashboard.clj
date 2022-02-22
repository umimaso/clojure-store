(ns clojure-store.routes.dashboard
  (:require
   [clojure-store.handlers :as handlers]
   [clojure-store.layout :as layout]
   [clojure-store.db.core :as db]
   [clojure.java.io :as io]
   [clojure-store.middleware :as middleware]
   [ring.util.response]
   [ring.util.http-response :as response]
   [clojure.string :as str]
   [struct.core :as st]
   [clojure.tools.logging :as log]))

(defn dashboard [{:keys [flash] :as request}]
  (layout/render
   request
   "dashboard.html"
   (merge
    {:stock (handlers/get-stock)}
    {:options (db/get-options)})))

(defn update-stock [{:keys [params]}]
  (log/debug params)
  ; Determine action sent
  (if (= (get params :action) "Remove Option")
    (db/remove-stock-limit-for-option-id!
      (hash-map :option_id (get params :option-id))))
  (if (= (get params :action) "Update Stock")
    (db/update-stock-for-option-id!
      (hash-map
      :stock_count (+
                    (if-let [stock
                              (get
                              (db/get-stock-for-option-id
                                {:option_id
                                (get params :option-id)})
                              :stock_count)]
                      stock
                      0)
                    (Integer/parseInt (get params :quantity))),
      :option_id (get params :option-id))))
  (response/found "/"))

(defn dashboard-routes []
  [""
   {:middleware [middleware/wrap-csrf
                 middleware/wrap-formats]}
   ["/" {:get dashboard}]
   ["/dashboard" {:get dashboard}]
   ["/dashboard/stock" {:post update-stock}]])
