-- :name get-tshirt-option-types :? :*
-- :doc retrieves all tshirt option types
SELECT
    id,
    tshirt_option_type_name
FROM tshirt_option_type WHERE is_deleted = 0;

-- :name get-tshirt-options :? :*
-- :doc retrieves all tshirt options
SELECT
    tshirt_option_type_id,
    tshirt_option_name
FROM tshirt_option WHERE is_deleted = 0;

-- :name get-tshirt-option-names-for-type :? :*
-- :doc retrieve values for a given tshirt option type
SELECT
    tshirt_option_name
FROM tshirt_option WHERE tshirt_option_type_id = :type_id AND is_deleted = 0;
