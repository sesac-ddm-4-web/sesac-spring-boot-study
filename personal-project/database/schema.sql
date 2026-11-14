
CREATE DATABASE IF NOT EXISTS product_management
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE product_management;

-- 캠핑 장비 / 위시리스트
CREATE TABLE IF NOT EXISTS collection_items (
                                                id BIGINT NOT NULL AUTO_INCREMENT,
                                                name VARCHAR(255) NOT NULL,
    price BIGINT NOT NULL,
    source_type VARCHAR(20) NOT NULL,
    external_id BIGINT NULL,
    stock INT NULL,

    PRIMARY KEY (id),

    UNIQUE KEY uk_source_external (
                                      source_type, external_id
                                  ),

    CONSTRAINT chk_collection_price CHECK (price >= 0),
    CONSTRAINT chk_collection_stock CHECK (
                                              stock IS NULL OR stock >= 0
                                          )
    );

-- 장바구니
CREATE TABLE IF NOT EXISTS cart_items (
                                          id BIGINT NOT NULL AUTO_INCREMENT,
                                          collection_item_id BIGINT NOT NULL,
                                          name VARCHAR(255) NOT NULL,
    unit_price BIGINT NOT NULL,
    stock INT NULL,
    quantity INT NOT NULL,

    PRIMARY KEY (id),

    UNIQUE KEY uk_cart_collection (collection_item_id),

    CONSTRAINT chk_cart_quantity CHECK (
                                           quantity BETWEEN 1 AND 5
                                       ),
    CONSTRAINT chk_cart_price CHECK (unit_price >= 0),
    CONSTRAINT chk_cart_stock CHECK (
                                        stock IS NULL OR stock >= 0
                                    )
    );
