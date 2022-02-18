CREATE TABLE tshirt_option_stock
(tshirt_option_id INTEGER PRIMARY KEY,
	stock_count INTEGER,
	FOREIGN KEY (tshirt_option_id) REFERENCES tshirt_option(id));
