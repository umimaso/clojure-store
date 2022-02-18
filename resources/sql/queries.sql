-- :name get-option-types :? :*
-- :doc retrieves all tshirt option types
SELECT
    id,
    tshirt_option_type_name
FROM tshirt_option_type WHERE is_deleted = 0;

-- :name get-options :? :*
-- :doc retrieves all tshirt options
SELECT
    tshirt_option_type.tshirt_option_type_name,
    tshirt_option.id,
    tshirt_option.tshirt_option_type_id,
    tshirt_option.tshirt_option_name,
    tshirt_option_stock.stock_count
FROM tshirt_option
    LEFT JOIN tshirt_option_stock
        ON tshirt_option.id = tshirt_option_stock.tshirt_option_id
    LEFT JOIN tshirt_option_type
        ON tshirt_option_type.id = tshirt_option.tshirt_option_type_id
WHERE tshirt_option.is_deleted = 0  AND tshirt_option_type.is_deleted = 0;
ORDER BY tshirt_option_type.tshirt_option_type_name;

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

-- :name get-stock :? :*
-- :doc retrieve stock records and associated option information
SELECT
    tshirt_option_type.tshirt_option_type_name,
    tshirt_option.tshirt_option_name,
    tshirt_option_stock.stock_count
FROM tshirt_option_stock
    LEFT JOIN tshirt_option
        ON tshirt_option.id = tshirt_option_stock.tshirt_option_id
    LEFT JOIN tshirt_option_type
        ON tshirt_option_type.id = tshirt_option.tshirt_option_type_id
WHERE tshirt_option.is_deleted = 0 AND tshirt_option_type.is_deleted = 0
ORDER BY tshirt_option_type.tshirt_option_type_name;

-- :name get-stock-for-option-id :? :1
-- :doc retrieve stock count for a given option id
SELECT stock_count
FROM tshirt_option_stock WHERE tshirt_option_id = :option_id;

-- :name update-stock-for-option-id! :! :n
-- :doc updates the stock count for a given option id to given amount
INSERT INTO tshirt_option_stock (
    tshirt_option_id,
    stock_count
) VALUES (:option_id, :stock_count)
ON CONFLICT (tshirt_option_id)
DO UPDATE SET stock_count=excluded.stock_count;

-- :name remove-stock-limit-for-option-id! :! :n
-- :doc remove an option from having a stock limit
DELETE
FROM tshirt_option_stock
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
returning *;

-- :name create-order-option! :! :n
-- :doc populate order option for associated order id
INSERT INTO tshirt_order_option (
    order_id,
    tshirt_option_type_id,
    tshirt_option_id,
    tshirt_option_value
) VALUES (:order_id, :tshirt_option_type_id, :tshirt_option_id, :tshirt_option_value);
