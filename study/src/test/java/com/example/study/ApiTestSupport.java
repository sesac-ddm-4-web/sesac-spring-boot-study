package com.example.study;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import tools.jackson.databind.ObjectMapper;

/** 실제 API를 호출해 상황을 만드는 통합 테스트 공통 도구. */
@IntegrationTest
public abstract class ApiTestSupport {

    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected ObjectMapper objectMapper;
    @Autowired
    protected JdbcTemplate jdbcTemplate;
    @Autowired
    private DatabaseCleaner databaseCleaner;

    @BeforeEach
    void cleanDatabase() {
        databaseCleaner.clean();
    }

    /** 가입하고 로그인한 세션. 이름은 이메일의 앞부분이다. */
    protected MockHttpSession loginAs(String name) throws Exception {
        String email = name + "@example.com";
        send(post("/api/members"), Map.of("email", email, "password", "password123", "name", name))
                .andExpect(status().isCreated());
        var result = send(post("/api/auth/login"), Map.of("email", email, "password", "password123"))
                .andExpect(status().isOk())
                .andReturn();
        return (MockHttpSession) result.getRequest().getSession(false);
    }

    protected static Map<String, Object> studyBody(String day, String start, String end) {
        return new HashMap<>(Map.of(
                "name", "알고리즘", "description", "매주 온라인으로 문제 풀이", "capacity", 4,
                "recruitStatus", "RECRUITING", "dayOfWeek", day, "startTime", start, "endTime", end));
    }

    protected ResultActions createStudy(MockHttpSession session, Map<String, Object> body) throws Exception {
        return send(post("/api/studies").session(session), body);
    }

    protected long createdStudyId(MockHttpSession session, Map<String, Object> body) throws Exception {
        String json = createStudy(session, body).andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(json).get("id").asLong();
    }

    protected ResultActions send(MockHttpServletRequestBuilder request, Object body) throws Exception {
        return mockMvc.perform(request
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));
    }

    protected long count(String table) {
        return jdbcTemplate.queryForObject("select count(*) from " + table, Long.class);
    }
}
