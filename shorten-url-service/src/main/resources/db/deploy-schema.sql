CREATE TABLE IF NOT EXISTS shorten_urls (
    id BIGINT NOT NULL AUTO_INCREMENT,
    original_url LONGTEXT NOT NULL,
    shorten_url_key VARCHAR(8) NOT NULL,
    redirect_count BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uk_shorten_urls_key UNIQUE (shorten_url_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS pdf_files (
                                         id BIGINT NOT NULL AUTO_INCREMENT,
                                         shorten_url_id BIGINT NOT NULL,
                                         original_file_name VARCHAR(255) NOT NULL,
    stored_file_name VARCHAR(255) NOT NULL,

    PRIMARY KEY (id),
    CONSTRAINT uk_pdf_files_stored_name UNIQUE (stored_file_name),
    CONSTRAINT fk_pdf_files_shorten_url
    FOREIGN KEY (shorten_url_id) REFERENCES shorten_urls(id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;