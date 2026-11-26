CREATE TABLE IF NOT EXISTS members (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, login_id VARCHAR(50) NOT NULL,
 password_hash VARCHAR(255) NOT NULL, display_name VARCHAR(50) NOT NULL,
 role VARCHAR(16) NOT NULL, created_at DATETIME(3) NOT NULL,
 CONSTRAINT uk_members_login_id UNIQUE(login_id),
 CONSTRAINT chk_members_role CHECK(role IN ('ADMIN','PRESENTER'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS study_sessions (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, session_no INT NOT NULL, title VARCHAR(150) NOT NULL,
 starts_at DATETIME(3) NOT NULL, notice TEXT NOT NULL, location VARCHAR(200), meeting_url VARCHAR(2048),
 preparation TEXT, created_by BIGINT NOT NULL, created_at DATETIME(3) NOT NULL, updated_at DATETIME(3) NOT NULL,
 CONSTRAINT uk_study_sessions_no UNIQUE(session_no),
 CONSTRAINT fk_study_sessions_creator FOREIGN KEY(created_by) REFERENCES members(id),
 CONSTRAINT chk_study_sessions_no CHECK(session_no>=1)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS session_presenters (
 session_id BIGINT NOT NULL, member_id BIGINT NOT NULL, created_at DATETIME(3) NOT NULL,
 PRIMARY KEY(session_id,member_id),
 CONSTRAINT fk_session_presenters_session FOREIGN KEY(session_id) REFERENCES study_sessions(id),
 CONSTRAINT fk_session_presenters_member FOREIGN KEY(member_id) REFERENCES members(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS materials (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, session_id BIGINT NOT NULL, author_id BIGINT NOT NULL,
 title VARCHAR(150) NOT NULL, original_url VARCHAR(2048) NOT NULL,
 created_at DATETIME(3) NOT NULL, updated_at DATETIME(3) NOT NULL,
 CONSTRAINT fk_materials_session FOREIGN KEY(session_id) REFERENCES study_sessions(id),
 CONSTRAINT fk_materials_author FOREIGN KEY(author_id) REFERENCES members(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS short_links (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, code VARCHAR(40) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
 session_id BIGINT, material_id BIGINT, redirect_count BIGINT NOT NULL DEFAULT 0, created_at DATETIME(3) NOT NULL,
 CONSTRAINT uk_short_links_code UNIQUE(code), CONSTRAINT uk_short_links_session UNIQUE(session_id),
 CONSTRAINT uk_short_links_material UNIQUE(material_id),
 CONSTRAINT fk_short_links_session FOREIGN KEY(session_id) REFERENCES study_sessions(id),
 CONSTRAINT fk_short_links_material FOREIGN KEY(material_id) REFERENCES materials(id),
 CONSTRAINT chk_short_links_count CHECK(redirect_count>=0),
 CONSTRAINT chk_short_links_target CHECK((session_id IS NOT NULL AND material_id IS NULL)
 OR (session_id IS NULL AND material_id IS NOT NULL))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
