(ns clojure-store.routes.services
  (:require
   [reitit.swagger :as swagger]
   [reitit.swagger-ui :as swagger-ui]
   [reitit.ring.coercion :as coercion]
   [reitit.coercion.spec :as spec-coercion]
   [reitit.ring.middleware.muuntaja :as muuntaja]
   [reitit.ring.middleware.multipart :as multipart]
   [reitit.ring.middleware.parameters :as parameters]
   [clojure-store.middleware.formats :as formats]
   [ring.util.http-response :refer :all]
   [clojure.java.io :as io]))

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

   ["/ping"
    {:get (constantly (ok {:message "pong"}))}]])
