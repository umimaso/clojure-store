(ns clojure-store.routes.order
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
              [st/required [st/member
                            (for [{:keys [tshirt_option_id]} (db/get-option-ids-for-type {:type_id id})]
                              (str tshirt_option_id))]]))))))

; If the custom image option was selected, ensure that the field isn't empty
(defn validate-order-image
  [params]
  ; Custom image is selected
  (if (= (get params :image) "Custom")
    ; Check if custom image url is blank
    (if-let [custom-image (str/blank? (get params :custom_image_url))]
      {:custom_image_url "this field is mandatory when custom image is selected"})))

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
                                   (db/get-stock-for-option-id {:option_id (get params (keyword (str/lower-case tshirt_option_type_name)))})
                                   :stock_count)]
                           ; Stock defined for option
                           ; Compare stock against quantity in order
                           (if (> quantity stock)
                             ; Higher quantity requested for option than in stock
                             {:tshirt_option "Item(s) selected not in stock"}))))]
      (if (not= result {})
        result))))

; Get option types for use within order form
(defn option-types []
  (for [{:keys [id tshirt_option_type_name]} (api/req api/get-option-types)]
    {:id id,
     :tshirt_option_type_name tshirt_option_type_name}))

; Get options for use within order form for dropdown values, and attributes
(defn options []
  (let [stock (api/req api/get-stock)]
    (for [{:keys [id tshirt_option_type_id tshirt_option_name]} (api/req api/get-options)]
      (let [stock_option (into {} (filter #(= (:tshirt_option_id %) id) stock))]
        {:id id,
         :tshirt_option_type_id tshirt_option_type_id,
         :tshirt_option_name tshirt_option_name,
         :stock_count (get stock_option :stock_count)}))))

;
; Routes
;
(defn order-form [{:keys [flash] :as request}]
  (layout/render
   request
   "order.html"
   (merge
    {:option-types (option-types)}
    {:options (options)}
    (select-keys flash [:errors :success :full_name :email :phone_number :shipping_address :custom_image_url :delivery_details :quantity]))))

(defn new-order [{:keys [params]}]
  (if-let [errors
           (merge
            (validate-order-schema params)
            (validate-order-image params)
            (validate-order-stock params))]
    ; Invalid order, reload page with error messages
    (assoc (response/found "/order") :flash (assoc params :errors errors))

    ; Validation was successful, add the order to the database
    (let [new-order
          (api/req-body api/new-order
                        (merge
                         params
                         {:price (*
                                  (Integer/parseInt (get params :quantity))
                                  (Double/parseDouble
                                   (get
                                    (db/get-price-for-quality {:option_id (get params :quality)})
                                    :tshirt_option_value))),
                          :delivered false}))]
      (let [options (api/req api/get-options)]
        (let [option-types (api/req api/get-option-types)]
          ; Add each tshirt option in the order to the database for the order id created
          ; Decrease stock for each tshirt option
          (doseq [{:keys [id tshirt_option_type_name]} option-types]
            (let [option-id (Integer/parseInt (get params (keyword (str/lower-case tshirt_option_type_name))))]
              (let [option (into {} (filter #(= (:id %) option-id) options))]
                (api/req-body api/new-order-option
                              {:order_id (get new-order :id),
                               :tshirt_option_type_id id,
                               :tshirt_option_id option-id,
                               :tshirt_option_value (if-let [custom-image
                                                             (if (= tshirt_option_type_name "Image")
                                                               (if (= (get params :image) "18") ; 18 is the Custom option id
                                                                 (get params :custom_image_url)))]
                                                      custom-image
                                                      (get option :tshirt_option_value))})

                (if-let [stock
                         (get
                          (db/get-stock-for-option-id {:option_id option-id})
                          :stock_count)]
                  (api/req-body api/update-stock-option
                                {:stock_count (-
                                               stock
                                               (Integer/parseInt (get params :quantity))),
                                 :tshirt_option_id option-id})))))))
      (assoc (response/found "/order") :flash (assoc params :success "true")))))

(defn order-routes []
  [""
   {:middleware [middleware/wrap-csrf
                 middleware/wrap-formats]}
   ["/order" {:get order-form
              :post new-order}]])
