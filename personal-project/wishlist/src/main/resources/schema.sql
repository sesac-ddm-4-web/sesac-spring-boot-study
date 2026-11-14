CREATE TABLE IF NOT EXISTS collection_items (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(100) NOT NULL,
    price       INT          NOT NULL,
    source_type VARCHAR(20)  NOT NULL,
    external_id BIGINT,
    stock       INT,
    PRIMARY KEY (id),
    CONSTRAINT uk_collection_items_source_external UNIQUE (source_type, external_id)
);

CREATE TABLE IF NOT EXISTS cart_items (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    wish_item_id BIGINT       NOT NULL,
    name         VARCHAR(100) NOT NULL,
    unit_price   BIGINT       NOT NULL,
    stock        INT,
    quantity     INT          NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_cart_items_wish_item UNIQUE (wish_item_id)
);
