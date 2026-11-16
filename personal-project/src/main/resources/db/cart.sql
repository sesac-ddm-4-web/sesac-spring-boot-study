CREATE TABLE cart_items (
                            id BIGINT NOT NULL,
                            item_id BIGINT NOT NULL,
                            name VARCHAR(255) NOT NULL,
                            unit_price BIGINT NOT NULL,
                            source_type VARCHAR(20) NOT NULL,
                            stock INT NULL,
                            quantity INT NOT NULL,

                            PRIMARY KEY (id),
                            CONSTRAINT uk_cart_items_item UNIQUE (item_id),
                            CONSTRAINT ck_cart_items_price CHECK (unit_price >= 0),
                            CONSTRAINT ck_cart_items_quantity CHECK (quantity BETWEEN 1 AND 5),
                            CONSTRAINT ck_cart_items_stock CHECK (stock IS NULL OR stock >= 0)
);

CREATE TABLE cart_state (
                            id BIGINT NOT NULL,
                            next_sequence BIGINT NOT NULL,

                            PRIMARY KEY (id),
                            CONSTRAINT ck_cart_state_id CHECK (id = 1),
                            CONSTRAINT ck_cart_state_sequence CHECK (next_sequence >= 1)
);

INSERT INTO cart_state (id, next_sequence)
VALUES (1, 1);