CREATE DATABASE IF NOT EXISTS url_management;

USE url_management;

CREATE TABLE IF NOT EXISTS shorten_urls (
                                            shorten_url_key VARCHAR(8) PRIMARY KEY,
    original_url VARCHAR(2048) NOT NULL,
    redirect_count BIGINT NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE
    );