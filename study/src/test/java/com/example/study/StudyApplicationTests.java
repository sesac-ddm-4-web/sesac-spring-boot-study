package com.example.study;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

@IntegrationTest
class StudyApplicationTests {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("테스트는 Docker MySQL의 테스트 전용 DB에 접속한다")
    void connectsToTestDatabase() {
        String database = jdbcTemplate.queryForObject("SELECT DATABASE()", String.class);

        assertThat(database).isEqualTo("study_test");
    }
}
