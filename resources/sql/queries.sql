-- :name get-option-types :? :*
-- :doc retrieves all tshirt option types
SELECT
    id,
    tshirt_option_type_name
FROM tshirt_option_type WHERE is_deleted = 0;

-- :name get-options :? :*
-- :doc retrieves all tshirt options
SELECT
    tshirt_option_type_id,
    tshirt_option_name
FROM tshirt_option WHERE is_deleted = 0;

-- :name get-option-names-for-type :? :*
-- :doc retrieve tshirt option names for a given tshirt option type
SELECT tshirt_option_name
FROM tshirt_option WHERE tshirt_option_type_id = :type_id AND is_deleted = 0;

-- :name get-option-for-type-and-name :? :1
-- :doc retrieve tshirt option for a given type and option name
SELECT
    id,
    tshirt_option_value
FROM tshirt_option WHERE tshirt_option_type_id = :type_id AND tshirt_option_name = :option_name AND is_deleted = 0;

-- :name get-stock-for-option-id :? :1
-- :doc retrieve stock count for a given option id
SELECT
    stock_count
FROM tshirt_option_stock WHERE tshirt_option_id = :option_id;

-- :name remove-stock-for-option-id! :! :n
-- :doc decrease the stock count for a given option id by given amount
UPDATE tshirt_option_stock
SET stock_count = :stock_count
WHERE tshirt_option_id = :option_id;

-- :name get-price-for-quality :? :1
-- :doc retrieve the price for a given tshirt quality
SELECT tshirt_option_value
FROM tshirt_option WHERE tshirt_option_type_id = 3 AND tshirt_option_name = :quality;

-- :name create-order! :<! :1
-- :doc create a new order for the given order parameters
INSERT INTO tshirt_order (
    full_name,
    email,
    phone_number,
    shipping_address,
    delivery_details,
    quantity,
    price,
    payment_success,
    delivered
) VALUES (:full_name, :email, :phone_number, :shipping_address, :delivery_details, :quantity, :price, :payment_success, :delivered)
RETURNING *;

-- :name create-order-option! :! :n
-- :doc populate order option for associated order id
INSERT INTO tshirt_order_option (
    order_id,
    tshirt_option_type_id,
    tshirt_option_id,
    tshirt_option_value
) VALUES (:order_id, :tshirt_option_type_id, :tshirt_option_id, :tshirt_option_value);
