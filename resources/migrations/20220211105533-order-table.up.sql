CREATE TABLE tshirt_order
(id INTEGER PRIMARY KEY,
    full_name TEXT,
    email TEXT,
    phone_number TEXT,
    shipping_address TEXT,
    delivery_details TEXT,
    quantity INTEGER,
    price TEXT,
    delivered BOOLEAN);
--;;
CREATE TABLE tshirt_order_option
(order_id INTEGER,
    tshirt_option_type_id INTEGER,
    tshirt_option_id INTEGER,
    tshirt_option_value TEXT, -- Store value of option at order time to prevent option changes affecting historic orders.
    FOREIGN KEY (order_id) REFERENCES tshirt_order(id),
    FOREIGN KEY (tshirt_option_type_id) REFERENCES tshirt_option_type(id),
    FOREIGN KEY (tshirt_option_id) REFERENCES tshirt_option(id),
    PRIMARY KEY (order_id, tshirt_option_type_id));
