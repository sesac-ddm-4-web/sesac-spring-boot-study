CREATE TABLE items (
                       id BIGINT NOT NULL AUTO_INCREMENT,
                       name VARCHAR(255) NOT NULL,
                       price INT NOT NULL,
                       source_type VARCHAR(20) NOT NULL,
                       external_id BIGINT NULL,
                       stock INT NULL,

                       PRIMARY KEY (id),
                       CONSTRAINT uk_items_external UNIQUE (source_type, external_id),
                       CONSTRAINT ck_items_price CHECK (price >= 0),
                       CONSTRAINT ck_items_stock CHECK (stock IS NULL OR stock >= 0)
);