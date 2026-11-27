package com.example.study;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/** FK의 자식 테이블부터 지운다. 테스트 DB에서만 사용한다. */
@Component
public class DatabaseCleaner {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseCleaner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void clean() {
        for (String table : new String[] { "study_history", "schedule", "study_member", "study", "member", "profile" }) {
            jdbcTemplate.update("delete from " + table);
        }
    }
}
