(ns clojure-store.routes.dashboard
  (:require
   [clojure-store.handlers :as api]
   [clojure-store.layout :as layout]
   [clojure-store.db.core :as db]
   [clojure.java.io :as io]
   [clojure-store.middleware :as middleware]
   [ring.util.response]
   [ring.util.http-response :as response]
   [clojure.string :as str]
   [struct.core :as st]
   [clojure.tools.logging :as log]))

;
; Helpers
;

(defn stock-table []
  (let [options (api/req api/get-options)]
    (let [option-types (api/req api/get-option-types)]

      (for [{:keys [tshirt_option_id stock_count]} (api/req api/get-stock)]
        (let [option (into {} (filter #(= (:id %) tshirt_option_id) options))]
          (let [option-type (into {} (filter #(= (:id %) (get option :tshirt_option_type_id)) option-types))]

            {:tshirt_option_type_name (get option-type :tshirt_option_type_name),
             :tshirt_option_name (get option :tshirt_option_name),
             :stock_count stock_count}))))))

(defn options-dropdown []
  (let [options (api/req api/get-options)]
    (let [option-types (api/req api/get-option-types)]

      (for [{:keys [id tshirt_option_type_id tshirt_option_name]} options]
        (let [option-type (into {} (filter #(= (:id %) tshirt_option_type_id) option-types))]

          {:id id
           :tshirt_option_type_name (get option-type :tshirt_option_type_name),
           :tshirt_option_name tshirt_option_name})))))

; Get option types for use in order table
(defn option-types []
  (for [{:keys [id tshirt_option_type_name]} (db/get-option-types-inc-del)]
    {:id id,
     :tshirt_option_type_name tshirt_option_type_name}))

;
; Routes
;
(defn dashboard [{:keys [flash] :as request}]
  (layout/render
   request
   "dashboard.html"
   (merge
    {:stock (stock-table)}
    {:options (options-dropdown)}
    {:option-types (option-types)}
    {:orders (if-let [orders (not-empty (select-keys flash [:found-orders]))]
               (get (select-keys flash [:found-orders]) :found-orders)
               (api/req api/get-orders))})))

(defn search-orders [{:keys [params]}]
  (if (= (get params :action) "Search")
    (let [orders
          (for [{:keys [id full_name email phone_number shipping_address delivery_details quantity price delivered]}
                (db/search-orders {:full_name (str "%" (get params :name) "%"),
                                   :email (str "%" (get params :email) "%"),
                                   :phone (str "%" (get params :phone) "%"),
                                   :shipping (str "%" (get params :shipping) "%"),
                                   :delivery (str "%" (get params :delivery) "%"),
                                   :quantity (str "%" (get params :quantity) "%"),
                                   :price (str "%" (get params :price) "%")})]
            {:id id,
             :full_name full_name,
             :email email,
             :phone_number phone_number,
             :shipping_address shipping_address,
             :delivery_details delivery_details,
             :quantity quantity,
             :price price,
             :delivered delivered,
             :tshirt_options (db/get-options-for-order {:option_id id})})]
      (assoc (response/found "/") :flash (assoc {} :found-orders orders)))
    (response/found "/")))

(defn update-stock [{:keys [params]}]
  ; Determine action sent
  (if (= (get params :action) "Remove Option")
    (api/req-body api/remove-stock-option {:tshirt_option_id (get params :option-id)}))
  (if (= (get params :action) "Update Stock")
    (api/req-body api/update-stock-option
                  {:tshirt_option_id (get params :option-id),
                   :stock_count (+
                                 (if-let [stock
                                          (get
                                           (db/get-stock-for-option-id {:option_id (get params :option-id)})
                                           :stock_count)]
                                   stock
                                   0)
                                 (Integer/parseInt (get params :quantity)))}))
  (response/found "/"))

(defn order-delivered [{:keys [params]}]
  ; Determine action sent
  (if (= (get params :action) "Set Undelivered")
    (api/req-body api/update-order-status {:order_id (get params :order-id), :delivered 0}))
  (if (= (get params :action) "Set Delivered")
    (api/req-body api/update-order-status {:order_id (get params :order-id), :delivered 1}))
  (response/found "/"))

(defn dashboard-routes []
  [""
   {:middleware [middleware/wrap-csrf
                 middleware/wrap-formats]}
   ["/" {:get dashboard}]
   ["/dashboard" {:get dashboard
                  :post search-orders}]
   ["/dashboard/stock" {:post update-stock}]
   ["/dashboard/order" {:post order-delivered}]])
