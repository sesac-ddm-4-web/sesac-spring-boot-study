CREATE TABLE IF NOT EXISTS wishlist_item
(
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(200) NOT NULL,
    price       INT          NOT NULL,
    source_type VARCHAR(20)  NOT NULL,
    external_id BIGINT       NULL,
    stock       INT          NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_wishlist_item_external_id UNIQUE (external_id)
);

CREATE TABLE IF NOT EXISTS cart
(
    id           BIGINT NOT NULL,
    last_item_id BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id)
);

INSERT IGNORE INTO cart (id, last_item_id) VALUES (1, 0);

CREATE TABLE IF NOT EXISTS cart_item
(
    id               BIGINT       NOT NULL,
    cart_id          BIGINT       NOT NULL,
    wishlist_item_id BIGINT       NOT NULL,
    name             VARCHAR(200) NOT NULL,
    unit_price       BIGINT       NOT NULL,
    quantity         INT          NOT NULL,
    stock_limit      INT          NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_cart_item_cart FOREIGN KEY (cart_id) REFERENCES cart (id),
    CONSTRAINT uk_cart_item_wishlist_item UNIQUE (cart_id, wishlist_item_id)
);