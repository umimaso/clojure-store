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

; Validate the order against expected schema
(defn validate-order-schema
  [params]
  (first (st/validate
          params
          (merge
           {:full_name [st/required st/string]
            :email [st/required st/email]
            :phone_number [st/required st/string]
            :shipping_address [st/required st/string]
            :delivery_details [st/string]
            :quantity [st/required st/integer-str] ; TODO: Prevent quantity accepting negative values
            :custom_image_url [st/string]}
          ; Dynamic schema generated from the database
           (zipmap
            (for
             [{:keys [tshirt_option_type_name]} (db/get-option-types)]
              (keyword (str/lower-case tshirt_option_type_name)))
            (for
             [{:keys [id]} (db/get-option-types)]
              [st/required [st/member (for
                                       [{:keys [tshirt_option_name]} (db/get-option-names-for-type {:type_id id})]
                                        tshirt_option_name)]]))))))

; If the custom image option was selected, ensure that the field isn't empty
(defn validate-order-image
  [params]
  ; Custom image is selected
  (if (= (get params :image) "Custom")
    ; Check if custom image url is blank
    (if-let [custom-image (str/blank? (get params :custom_image_url))]
      (hash-map :custom_image_url "this field is mandatory when custom image is selected"))))

; Validate that the order options selected are in stock
(defn validate-order-stock
  [params]
  ; For each option type get id of the selected option in the order
  ; With the selected option id, get the stock count for it
  ; Check if stock is more than quantity requested
  ; If more quantity is requested than the stock we have than return error
  (let [quantity (Integer/parseInt (get params :quantity))]
    (let [result (into {}
                       (for [{:keys [tshirt_option_type_name id]} (db/get-option-types)]
                         (if-let [stock
                                  (get
                                   (db/get-stock-for-option-id
                                    {:option_id
                                     (get
                                      (db/get-option-for-type-and-name
                                       {:type_id id,
                                        :option_name (get params (keyword (str/lower-case tshirt_option_type_name)))})
                                      :id)})
                                   :stock_count)]
                           ; Stock defined for option
                           ; Compare stock against quantity in order
                           (if (> quantity stock)
                             ; Higher quantity requested for option than in stock
                             (hash-map :tshirt_option "Item(s) selected not in stock")))))]
      (if (not= result {})
        result))))

;
; Routes
;
(defn order-form [{:keys [flash] :as request}]
  (layout/render
   request
   "order-form.html"
   (merge
    {:option-types (db/get-option-types)}
    {:options (db/get-options)}
    (select-keys flash [:errors :full_name :email :phone_number :shipping_address :custom_image_url :delivery_details :quantity]))))

(defn new-order [{:keys [params]}]
  (if-let [errors
           (merge
            (validate-order-schema params)
            (validate-order-image params)
            (validate-order-stock params))]
    (->
     (response/found "/order")
     (assoc :flash (assoc params :errors errors)))
    (do
      ; Validation was successful, add the order to the database
      (if-let [new-order (db/create-order!
                          (merge
                           params
                           (hash-map
                            :price (*
                                    (Integer/parseInt (get params :quantity))
                                    (Double/parseDouble
                                     (get
                                      (db/get-price-for-quality {:quality (get params :quality)})
                                      :tshirt_option_value))),
                            :payment_success true,
                            :delivered false)))]
        (do
          ; Add each tshirt option in the order to the database for the order id created
          ; Decrease stock for each tshirt option
          (doseq [{:keys [tshirt_option_type_name id]} (db/get-option-types)]
            (db/create-order-option!
             (hash-map
              :order_id (get new-order :id),
              :tshirt_option_type_id id,
              :tshirt_option_id (get
                                 (db/get-option-for-type-and-name
                                  {:type_id id,
                                   :option_name (get params (keyword (str/lower-case tshirt_option_type_name)))})
                                 :id),
              :tshirt_option_value (if-let [custom-image
                                            (if (= tshirt_option_type_name "Image")
                                              (if (= (get params :image) "Custom")
                                                (get params :custom_image_url)))]
                                     custom-image
                                     (get
                                      (db/get-option-for-type-and-name
                                       {:type_id id,
                                        :option_name (get params (keyword (str/lower-case tshirt_option_type_name)))})
                                      :tshirt_option_value))))
            (let [option_id
                  (get
                   (db/get-option-for-type-and-name
                    {:type_id id,
                     :option_name (get params (keyword (str/lower-case tshirt_option_type_name)))})
                   :id)]
              (if-let [stock
                       (get
                        (db/get-stock-for-option-id
                         {:option_id option_id})
                        :stock_count)]
                (db/update-stock-for-option-id!
                 (hash-map
                  :stock_count (-
                                stock
                                (Integer/parseInt (get params :quantity))),
                  :option_id option_id)))))))

      ; TODO: Display order confirmation after adding order to database
      (response/found "/order/confirmation"))))

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
