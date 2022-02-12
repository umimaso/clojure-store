CREATE TABLE tshirt_option_type
(id INTEGER PRIMARY KEY,
    tshirt_option_type_name TEXT,
    is_deleted BOOLEAN DEFAULT 0);
--;;
CREATE TABLE tshirt_option
(id INTEGER PRIMARY KEY,
    tshirt_option_type_id INTEGER,
    tshirt_option_name TEXT,
    tshirt_option_value TEXT,
    is_deleted BOOLEAN DEFAULT 0,
    FOREIGN KEY (tshirt_option_type_id) REFERENCES tshirt_option_type(id));
--;;
INSERT INTO tshirt_option_type (id, tshirt_option_type_name) VALUES (1, "Size"),
(2, "Colour"),
(3, "Quality"),
(4, "Image");
--;;
INSERT INTO tshirt_option (
    tshirt_option_type_id, tshirt_option_name, tshirt_option_value
) VALUES (1, "Small", "S"),
(1, "Medium", "M"),
(1, "Large", "L"),
(1, "Extra Large", "XL"),
(1, "Extra Extra Large", "XXL"),
(2, "Red", "ff0000"),
(2, "Orange", "ffa500"),
(2, "Yellow", "ffff00"),
(2, "Green", "00ff00"),
(2, "Cyan", "00ffff"),
(2, "Blue", "0000ff"),
(2, "Violet", "ee82ee"),
(3, "Standard", "9.99"),
(3, "Supreme", "15.99"),
(4, "Clojure", "https://upload.wikimedia.org/wikipedia/commons/5/5d/Clojure_logo.svg"),
(4, "Python", "https://www.python.org/static/community_logos/python-logo-generic.svg"),
(4, "Rust", "https://upload.wikimedia.org/wikipedia/commons/d/d5/Rust_programming_language_black_logo.svg"),
(4, "Custom", NULL);
