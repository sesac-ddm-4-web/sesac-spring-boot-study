CREATE TABLE shorten_urls
(
    shorten_url_key CHAR(8) PRIMARY KEY,
    original_url    VARCHAR(2048) NOT NULL,
    redirect_count  BIGINT        NOT NULL,
    expires_at      TIMESTAMP(6)
);

CREATE INDEX idx_shorten_url_expires_at
    ON shorten_urls (expires_at);