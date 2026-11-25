CREATE TABLE shorten_urls
(
    shorten_url_key CHAR(8) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    original_url    VARCHAR(2048)                                 NOT NULL,
    redirect_count  BIGINT                                        NOT NULL,
    expires_at      DATETIME(6) NULL,
    PRIMARY KEY (shorten_url_key),
    INDEX           idx_shorten_url_expires_at (expires_at)
);