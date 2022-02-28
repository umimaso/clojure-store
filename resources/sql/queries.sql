-- Order queries
-- :name get-orders :? :*
-- :doc get all orders with their associated options
SELECT
    email,
    phone_number,
    delivery_details,
    delivered,
    id,
    full_name,
    quantity,
    price,
    shipping_address
FROM tshirt_order;

-- :name get-options-for-order :? :*
-- :doc get all order options for a given order id
SELECT
    tshirt_option_type_id,
    tshirt_option_id,
    tshirt_option_value
FROM tshirt_order_option
WHERE order_id = :option_id;

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
    delivered
) VALUES (:full_name, :email, :phone_number, :shipping_address, :delivery_details, :quantity, :price, 0)
returning *;

-- :name create-order-option! :! :n
-- :doc populate order option for associated order id
INSERT INTO tshirt_order_option (
    order_id,
    tshirt_option_type_id,
    tshirt_option_id,
    tshirt_option_value
) VALUES (:order_id, :tshirt_option_type_id, :tshirt_option_id, :tshirt_option_value);

-- :name update-order-status! :! :n
-- :doc update the delivered status for associated order id
UPDATE tshirt_order
SET delivered = :delivered
WHERE id = :order_id;

-- Stock queries
-- :name get-stock :? :*
-- :doc retrieve stock records and associated option information
SELECT
    tshirt_option.id AS tshirt_option_id,
    tshirt_option_stock.stock_count
FROM tshirt_option_stock
    LEFT JOIN tshirt_option
        ON tshirt_option.id = tshirt_option_stock.tshirt_option_id
    LEFT JOIN tshirt_option_type
        ON tshirt_option_type.id = tshirt_option.tshirt_option_type_id
WHERE tshirt_option.is_deleted = 0 AND tshirt_option_type.is_deleted = 0
ORDER BY tshirt_option.id;

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


-- Option queries
-- :name get-options :? :*
-- :doc retrieves all tshirt options
SELECT
    tshirt_option.id,
    tshirt_option.tshirt_option_type_id,
    tshirt_option.tshirt_option_name,
    tshirt_option.tshirt_option_value
FROM tshirt_option
    LEFT JOIN tshirt_option_type
        ON tshirt_option_type.id = tshirt_option.tshirt_option_type_id
WHERE tshirt_option.is_deleted = 0  AND tshirt_option_type.is_deleted = 0
ORDER BY tshirt_option.id;

-- :name get-option-types :? :*
-- :doc retrieves all tshirt option types
SELECT
    id,
    tshirt_option_type_name
FROM tshirt_option_type WHERE is_deleted = 0;

-- :name get-option-types-inc-del :? :*
-- :doc retrieves all tshirt option types
SELECT
    id,
    tshirt_option_type_name
FROM tshirt_option_type;

-- :name get-option-ids-for-type :? :*
-- :doc retrieve tshirt option names for a given tshirt option type
SELECT id AS tshirt_option_id
FROM tshirt_option WHERE tshirt_option_type_id = :type_id AND is_deleted = 0;

-- :name get-price-for-quality :? :1
-- :doc retrieve the price for a given tshirt quality
SELECT tshirt_option_value
FROM tshirt_option WHERE tshirt_option_type_id = 3 AND id = :option_id;
