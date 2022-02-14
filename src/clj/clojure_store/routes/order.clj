(ns clojure-store.routes.order
  (:require
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

; Unused, hardcoded order schema
;(def order-schema
;  {:full_name [st/required st/string]
;   :email [st/required st/email]
;   :phone_number [st/required st/string]
;   :shipping_address [st/required st/string]
;   :delivery_details [st/string]
;   :quantity [st/required st/integer-str]
;   :size [st/required [st/member ["Small" "Medium" "Large" "Extra Large" "Extra Extra Large"]]]
;   :colour [st/required [st/member ["Red" "Orange" "Yellow" "Green" "Cyan" "Blue" "Violet"]]]
;   :quality [st/required [st/member ["Standard" "Supreme"]]]
;   :image [st/required [st/member ["Clojure" "Python" "Rust" "Custom"]]]})

(defn validate-order
  [params]
  (first (st/validate
          params
          (merge
           {:full_name [st/required st/string]
            :email [st/required st/email]
            :phone_number [st/required st/string]
            :shipping_address [st/required st/string]
            :delivery_details [st/string]
            :quantity [st/required st/integer-str]}
          ; Dynamic schema generated from the database
           (zipmap
            (for
             [{:keys [tshirt_option_type_name]} (db/get-tshirt-option-types)]
              (keyword (str/lower-case tshirt_option_type_name)))
            (for
             [{:keys [id]} (db/get-tshirt-option-types)]
              [st/required [st/member (for
                                       [{:keys [tshirt_option_name]} (db/get-tshirt-option-names-for-type {:type_id id})]
                                        tshirt_option_name)]]))))))

;
; Routes
;
(defn order-form [request]
  (layout/render
   request
   "order-form.html"
   (merge
    {:option-types (db/get-tshirt-option-types)}
    {:options (db/get-tshirt-options)})))

; TODO: Display errors on order page
; TODO: Add new order to database if order is successful
; TODO: Display order confirmation after adding order to database
(defn new-order [{:keys [params]}]
  (if-let [errors (validate-order params)]
    (-> (response/found "/order")
        (log/debug errors))
    (-> (response/found "/order/confirmation"))))

; TODO: Display confirmed order id in template
(defn order-confirmation [request]
  (layout/render
   request
   "order-confirmation.html"))

(defn order-routes []
  [""
   {:middleware [middleware/wrap-csrf
                 middleware/wrap-formats]}
   ["/order" {:get order-form
              :post new-order}]
   ["/order/confirmation" {:get order-confirmation}]])
