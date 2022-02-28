(ns clojure-store.routes.services
  (:require
   [clojure-store.handlers :as api]
   [reitit.swagger :as swagger]
   [reitit.swagger-ui :as swagger-ui]
   [reitit.ring.coercion :as coercion]
   [reitit.coercion.spec :as spec-coercion]
   [reitit.ring.middleware.muuntaja :as muuntaja]
   [reitit.ring.middleware.multipart :as multipart]
   [reitit.ring.middleware.parameters :as parameters]
   [clojure-store.middleware.formats :as formats]
   [ring.util.http-response :refer :all]
   [clojure.java.io :as io]
   [clojure.spec.alpha :as s]
   [clojure.tools.logging :as log]))

; API Specifications
(s/def :orders/id int?)
(s/def :orders/full_name string?)
(s/def :orders/email string?)
(s/def :orders/phone_number string?)
(s/def :orders/shipping_address string?)
(s/def :orders/delivery_details string?)
(s/def :orders/quantity int?)
(s/def :orders/price string?)
(s/def :orders/delivered int?)
(s/def :orders/tshirt_options (s/coll-of (s/keys :opt-un [:order-option/order_id :order-option/tshirt_option_type_id :order-option/tshirt_option_id :order-option/tshirt_option_value])))
(s/def ::orders-response (s/coll-of (s/keys :req-un [:orders/id :orders/full_name :orders/email :orders/phone_number :orders/shipping_address :orders/delivery_details :orders/quantity :orders/price :orders/delivered] :opt-un [:orders/tshirt_options])))
(s/def ::orders-post-request (s/keys :req-un [:orders/full_name :orders/email :orders/phone_number :orders/shipping_address :orders/delivery_details :orders/quantity :orders/price]))
(s/def ::orders-post-response (s/keys :req-un [:orders/id]))
(s/def ::orders-patch-request (s/keys :req-un [:orders/id :orders/delivered]))

(s/def :order-option/order_id int?)
(s/def :order-option/tshirt_option_type_id int?)
(s/def :order-option/tshirt_option_id int?)
(s/def :order-option/tshirt_option_value string?)
(s/def ::order-option-post-request (s/keys :req-un [:order-option/order_id :order-option/tshirt_option_type_id :order-option/tshirt_option_id :order-option/tshirt_option_value]))
(s/def ::order-option-response (s/coll-of (s/keys :req-un [:order-option/order_id :order-option/tshirt_option_type_id :order-option/tshirt_option_id :order-option/tshirt_option_value])))

(s/def :stock/tshirt_option_id int?)
(s/def :stock/stock_count int?)
(s/def ::stock-post-request (s/keys :req-un [:stock/tshirt_option_id :stock/stock_count]))
(s/def ::stock-delete-request (s/keys :req-un [:stock/tshirt_option_id]))
(s/def ::stock-response (s/coll-of (s/keys :req-un [:stock/tshirt_option_id :stock/stock_count])))

(s/def :options/id int?)
(s/def :options/tshirt_option_type_id int?)
(s/def :options/tshirt_option_name string?)
(s/def :options/tshirt_option_value string?)
(s/def ::options-response (s/coll-of (s/keys :req-un [:options/id :options/tshirt_option_type_id :options/tshirt_option_name :options/tshirt_option_value])))

(s/def :option-type/id int?)
(s/def :option-type/tshirt_option_type_name string?)
(s/def ::option-types-response (s/coll-of (s/keys :req-un [:option-type/id :option-type/tshirt_option_type_name])))

(defn service-routes []
  ["/api"
   {:coercion spec-coercion/coercion
    :muuntaja formats/instance
    :swagger {:id ::api}
    :middleware [; query-params & form-params
                 parameters/parameters-middleware
                 ; content-negotiation
                 muuntaja/format-negotiate-middleware
                 ; Encoding response body
                 muuntaja/format-response-middleware
                 ; Exception handling
                 coercion/coerce-exceptions-middleware
                 ; Decoding request body
                 muuntaja/format-request-middleware
                 ; Coercing response bodys
                 coercion/coerce-response-middleware
                 ; Coercing request parameters
                 coercion/coerce-request-middleware
                 ; Multipart
                 multipart/multipart-middleware]}

   ; Swagger documentation
   ["" {:no-doc true
        :swagger {:info {:title "clojure-store"}}}

    ["/swagger.json"
     {:get (swagger/create-swagger-handler)}]

    ["/api-docs/*"
     {:get (swagger-ui/create-swagger-ui-handler
            {:url "/api/swagger.json"
             :config {:validator-url nil}})}]]

   ["/orders"
    {:swagger {:tags ["orders"]}}

    [""
     {:get {:summary "return orders"
            :responses {200 {:body ::orders-response}}
            :handler api/get-orders}
      :post {:summary "add a new order"
             :parameters {:body ::orders-post-request}
             :responses {201 {:body ::orders-post-response}}
             :handler api/new-order}
      :patch {:summary "update delivered status for an order"
              :parameters {:body ::orders-patch-request}
              :responses {204 {}}
              :handler api/update-order-status}}]

    ["/option"
     {:post {:summary "add an order option to an order"
             :parameters {:body ::order-option-post-request}
             :responses {201 {:body ::order-option-response}}
             :handler api/new-order-option}}]]

   ["/stock"
    {:swagger {:tags ["stock"]}}

    [""
     {:get {:summary "return stock limits"
            :responses {200 {:body ::stock-response}}
            :handler api/get-stock}
      :put {:summary "update stock limit for option id"
            :parameters {:body ::stock-post-request}
            :responses {201 {:body ::stock-response}}
            :handler api/update-stock-option}
      :delete {:summary "remove stock limit for option id"
               :parameters {:body ::stock-delete-request}
               :responses {204 {}}
               :handler api/remove-stock-option}}]]

   ["/options"
    {:swagger {:tags ["options"]}}

    [""
     {:get {:summary "return list of options"
            :responses {200 {:body ::options-response}}
            :handler api/get-options}}]

    ["/types"
     {:get {:summary "return list of option types"
            :responses {200 {:body ::option-types-response}}
            :handler api/get-option-types}}]]])
