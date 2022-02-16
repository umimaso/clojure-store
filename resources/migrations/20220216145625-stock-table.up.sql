CREATE TABLE tshirt_option_stock
(id INTEGER PRIMARY KEY,
    tshirt_option_id INTEGER,
	stock_count INTEGER,
	FOREIGN KEY (tshirt_option_id) REFERENCES tshirt_option(id));
