package com.sesac.studyhub.support;

import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.mysql.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.List;

/**
 * 통합 테스트 공통 설정.
 * - Testcontainers로 테스트 전용 MySQL을 띄운다 (개발 DB와 분리).
 * - 컨테이너는 static 블록에서 한 번만 띄워 모든 통합 테스트가 공유한다 (싱글톤 컨테이너).
 * - @ServiceConnection이 컨테이너 접속 정보를 datasource 설정으로 자동 연결한다.
 * - 테스트마다 끝나고 테이블을 비워 테스트끼리 영향을 주지 않게 한다.
 */
@SpringBootTest(properties = "app.seed.enabled=false")
@AutoConfigureMockMvc
public abstract class IntegrationTestSupport {

    @ServiceConnection
    static final MySQLContainer MYSQL = new MySQLContainer(DockerImageName.parse("mysql:8.4"));

    static {
        MYSQL.start();
    }

    // FK 순서대로 (자식 → 부모) 삭제
    private static final List<String> TABLES = List.of("schedule", "study_member", "study", "member", "profile");

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @AfterEach
    void cleanUpDatabase() {
        TABLES.forEach(table -> jdbcTemplate.update("delete from " + table));
    }
}