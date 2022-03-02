(ns clojure-store.db.core-test
  (:require
   [clojure-store.db.core :refer [*db*] :as db]
   [luminus-migrations.core :as migrations]
   [clojure.test :refer :all]
   [next.jdbc :as jdbc]
   [conman.core :as conman]
   [mount.core :as mount]
   [clojure-store.config :refer [env]]
   [clojure.tools.logging :as log]))

(use-fixtures
  :once
  (fn [f]
    (mount/start
     #'clojure-store.config/env
     #'clojure-store.db.core/*db*)
    (migrations/migrate ["migrate"] (select-keys env [:database-url]))
    (f)))

;
; Helpers
;
(defn create-order []
  ; Create new order
  (db/create-order!
   {:full_name "John Doe",
    :email "john.doe@example.com",
    :phone_number "10000 200 300",
    :shipping_address "address, 5, asdf",
    :delivery_details "",
    :quantity "1",
    :price "9.99"}))

;
; Tests
;

; Create new order
(deftest test-order-create
  (conman/with-transaction [*db* {:rollback-only true}]
    (is (= {:id 1}
           (create-order)))))

; Get all orders
(deftest test-order-get
  (conman/with-transaction [*db* {:rollback-only true}]
    (create-order)
    (is (= [{:id 1
             :full_name "John Doe",
             :email "john.doe@example.com",
             :phone_number "10000 200 300",
             :shipping_address "address, 5, asdf",
             :delivery_details "",
             :quantity 1,
             :price "9.99",
             :delivered 0}]
           (db/get-orders)))))

; Search orders
(deftest test-order-search
  (conman/with-transaction [*db* {:rollback-only true}]
    (create-order)
    (is (= [{:id 1
             :full_name "John Doe",
             :email "john.doe@example.com",
             :phone_number "10000 200 300",
             :shipping_address "address, 5, asdf",
             :delivery_details "",
             :quantity 1,
             :price "9.99",
             :delivered 0}]
           (db/search-orders
            {:full_name "%ohn%",
             :email "%john.doe%",
             :phone "",
             :shipping "",
             :delivery "",
             :quantity "",
             :price ""})))))

; Update order status
(deftest test-order-update-status
  (conman/with-transaction [*db* {:rollback-only true}]
    (create-order)
    (db/update-order-status! {:order_id 1, :delivered 1})
    (is (= [{:id 1
             :full_name "John Doe",
             :email "john.doe@example.com",
             :phone_number "10000 200 300",
             :shipping_address "address, 5, asdf",
             :delivery_details "",
             :quantity 1,
             :price "9.99",
             :delivered 1}]
           (db/get-orders)))))

; Create new order option
(deftest test-order-option-create
  (conman/with-transaction [*db* {:rollback-only true}]
    (create-order)
    (is (= 1
           (db/create-order-option!
            {:order_id 1,
             :tshirt_option_type_id 1, ; Size
             :tshirt_option_id 3, ; Large
             :tshirt_option_value "L"})))))

; Get stock
(deftest test-stock-get
  (conman/with-transaction [*db* {:rollback-only true}]
    (db/update-stock-for-option-id! {:option_id 1, :stock_count 10})
    (is (= [{:tshirt_option_id 1
             :stock_count 10}]
           (db/get-stock)))))

; Get stock for option id
(deftest test-stock-get-for-option-id
  (conman/with-transaction [*db* {:rollback-only true}]
    (db/update-stock-for-option-id! {:option_id 1, :stock_count 10})
    (is (= {:stock_count 10}
           (db/get-stock-for-option-id {:option_id 1})))))

; Remove stock limit for option id
(deftest test-stock-remove-stock-limit
  (conman/with-transaction [*db* {:rollback-only true}]
    (db/update-stock-for-option-id! {:option_id 1, :stock_count 10})
    (db/remove-stock-limit-for-option-id! {:option_id 1})
    (is (= nil
           (db/get-stock-for-option-id {:option_id 1})))))

; Get options
(deftest test-options-get
  (conman/with-transaction [*db* {:rollback-only true}]
    (is (= [{:id 1,
             :tshirt_option_type_id 1,
             :tshirt_option_name "Small",
             :tshirt_option_value "S"}
            {:id 2,
             :tshirt_option_type_id 1,
             :tshirt_option_name "Medium",
             :tshirt_option_value "M"}
            {:id 3,
             :tshirt_option_type_id 1,
             :tshirt_option_name "Large",
             :tshirt_option_value "L"}
            {:id 4,
             :tshirt_option_type_id 1,
             :tshirt_option_name "Extra Large",
             :tshirt_option_value "XL"}
            {:id 5,
             :tshirt_option_type_id 1,
             :tshirt_option_name "Extra Extra Large",
             :tshirt_option_value "XXL"}
            {:id 6,
             :tshirt_option_type_id 2,
             :tshirt_option_name "Red",
             :tshirt_option_value "ff0000"}
            {:id 7,
             :tshirt_option_type_id 2,
             :tshirt_option_name "Orange",
             :tshirt_option_value "ffa500"}
            {:id 8,
             :tshirt_option_type_id 2,
             :tshirt_option_name "Yellow",
             :tshirt_option_value "ffff00"}
            {:id 9,
             :tshirt_option_type_id 2,
             :tshirt_option_name "Green",
             :tshirt_option_value "00ff00"}
            {:id 10,
             :tshirt_option_type_id 2,
             :tshirt_option_name "Cyan",
             :tshirt_option_value "00ffff"}
            {:id 11,
             :tshirt_option_type_id 2,
             :tshirt_option_name "Blue",
             :tshirt_option_value "0000ff"}
            {:id 12,
             :tshirt_option_type_id 2,
             :tshirt_option_name "Violet",
             :tshirt_option_value "ee82ee"}
            {:id 13,
             :tshirt_option_type_id 3,
             :tshirt_option_name "Standard",
             :tshirt_option_value "9.99"}
            {:id 14,
             :tshirt_option_type_id 3,
             :tshirt_option_name "Supreme",
             :tshirt_option_value "15.99"}
            {:id 15,
             :tshirt_option_type_id 4,
             :tshirt_option_name "Clojure",
             :tshirt_option_value
             "https://upload.wikimedia.org/wikipedia/commons/5/5d/Clojure_logo.svg"}
            {:id 16,
             :tshirt_option_type_id 4,
             :tshirt_option_name "Python",
             :tshirt_option_value
             "https://www.python.org/static/community_logos/python-logo-generic.svg"}
            {:id 17,
             :tshirt_option_type_id 4,
             :tshirt_option_name "Rust",
             :tshirt_option_value
             "https://upload.wikimedia.org/wikipedia/commons/d/d5/Rust_programming_language_black_logo.svg"}
            {:id 18,
             :tshirt_option_type_id 4,
             :tshirt_option_name "Custom",
             :tshirt_option_value "Custom"}]
           (db/get-options)))))

; Get option types
(deftest test-option-types-get
  (conman/with-transaction [*db* {:rollback-only true}]
    (is (= [{:id 1, :tshirt_option_type_name "Size"}
            {:id 2, :tshirt_option_type_name "Colour"}
            {:id 3, :tshirt_option_type_name "Quality"}
            {:id 4, :tshirt_option_type_name "Image"}]
           (db/get-option-types)))))

; Get option ids for a given type
(deftest test-option-ids-get-for-type
  (conman/with-transaction [*db* {:rollback-only true}]
    (is (= [{:tshirt_option_id 1}
            {:tshirt_option_id 2}
            {:tshirt_option_id 3}
            {:tshirt_option_id 4}
            {:tshirt_option_id 5}]
           (db/get-option-ids-for-type {:type_id 1})))))

; Get option value for option id
(deftest test-option-value-get-for-option-id
  (conman/with-transaction [*db* {:rollback-only true}]
    (is (= {:tshirt_option_value "S"}
           (db/get-option-value-for-option-id {:option_id 1})))))
