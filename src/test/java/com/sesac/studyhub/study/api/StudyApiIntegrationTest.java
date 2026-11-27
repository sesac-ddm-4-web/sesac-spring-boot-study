package com.sesac.studyhub.study.api;

import com.jayway.jsonpath.JsonPath;
import com.sesac.studyhub.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 실제 MySQL(Testcontainers) + Security 필터 + 컨트롤러~DB 전체를 거치는 API 통합 테스트.
 */
class StudyApiIntegrationTest extends IntegrationTestSupport {

    @Test
    @DisplayName("스터디를 개설하면 개설자가 스터디장으로 자동 참여한다")
    void createStudyMakesLeader() throws Exception {
        String leader = signupAndLogin("leader@test.com", "리더");
        long studyId = createStudy(leader, "JPA 스터디", 5);

        mockMvc.perform(get("/api/studies/{id}", studyId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.currentMemberCount").value(1))
            .andExpect(jsonPath("$.leader.name").value("리더"));

        mockMvc.perform(auth(get("/api/studies/{id}/members", studyId), leader))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].role").value("LEADER"));
    }

    @Test
    @DisplayName("같은 스터디에 두 번 참여하면 409 ALREADY_JOINED")
    void duplicateJoin() throws Exception {
        String leader = signupAndLogin("leader@test.com", "리더");
        String member = signupAndLogin("member@test.com", "멤버");
        long studyId = createStudy(leader, "JPA 스터디", 5);

        join(member, studyId).andExpect(status().isCreated());

        join(member, studyId)
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("ALREADY_JOINED"));
    }

    @Test
    @DisplayName("정원이 가득 차면 409 STUDY_FULL")
    void studyFull() throws Exception {
        String leader = signupAndLogin("leader@test.com", "리더");
        String first = signupAndLogin("first@test.com", "첫째");
        String second = signupAndLogin("second@test.com", "둘째");
        long studyId = createStudy(leader, "정원 2명 스터디", 2);   // 스터디장 포함 2명

        join(first, studyId)
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.currentMemberCount").value(2));

        join(second, studyId)
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("STUDY_FULL"));
    }

    @Test
    @DisplayName("스터디장은 탈퇴할 수 없고, 멤버는 탈퇴 후 다시 참여할 수 있다")
    void leaveRules() throws Exception {
        String leader = signupAndLogin("leader@test.com", "리더");
        String member = signupAndLogin("member@test.com", "멤버");
        long studyId = createStudy(leader, "JPA 스터디", 5);
        join(member, studyId).andExpect(status().isCreated());

        mockMvc.perform(auth(delete("/api/studies/{id}/members/me", studyId), leader))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("LEADER_CANNOT_LEAVE"));

        mockMvc.perform(auth(delete("/api/studies/{id}/members/me", studyId), member))
            .andExpect(status().isNoContent());

        join(member, studyId).andExpect(status().isCreated());   // hard delete라 재참여 가능
    }

    @Test
    @DisplayName("일정은 스터디장만 등록할 수 있다 (멤버는 403)")
    void onlyLeaderCreatesSchedule() throws Exception {
        String leader = signupAndLogin("leader@test.com", "리더");
        String member = signupAndLogin("member@test.com", "멤버");
        long studyId = createStudy(leader, "JPA 스터디", 5);
        join(member, studyId).andExpect(status().isCreated());

        createSchedule(member, studyId)
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value("NOT_STUDY_LEADER"));

        createSchedule(leader, studyId)
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.title").value("1주차"));
    }

    @Test
    @DisplayName("토큰이 없으면 401 UNAUTHORIZED, 잘못된 토큰이면 401 INVALID_TOKEN")
    void authenticationRequired() throws Exception {
        String body = studyJson("JPA 스터디", 5);

        mockMvc.perform(post("/api/studies").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));

        mockMvc.perform(auth(post("/api/studies"), "wrong.token.value")
                .contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("INVALID_TOKEN"));
    }

    @Test
    @DisplayName("잘못된 입력이면 400 INVALID_INPUT과 필드별 오류를 돌려준다")
    void validationError() throws Exception {
        String leader = signupAndLogin("leader@test.com", "리더");

        mockMvc.perform(auth(post("/api/studies"), leader)
                .contentType(MediaType.APPLICATION_JSON)
                .content(studyJson("", 1)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
            .andExpect(jsonPath("$.errors[*].field", containsInAnyOrder("name", "capacity")));
    }

    @Test
    @DisplayName("스터디 목록은 로그인 없이 조회되고, 스터디별 인원이 집계된다")
    void listStudiesWithoutLogin() throws Exception {
        String leader = signupAndLogin("leader@test.com", "리더");
        String member = signupAndLogin("member@test.com", "멤버");
        createStudy(leader, "혼자 스터디", 5);
        long popular = createStudy(leader, "인기 스터디", 5);
        join(member, popular).andExpect(status().isCreated());

        mockMvc.perform(get("/api/studies"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(2))
            .andExpect(jsonPath("$.content[?(@.name == '혼자 스터디')].currentMemberCount", contains(1)))
            .andExpect(jsonPath("$.content[?(@.name == '인기 스터디')].currentMemberCount", contains(2)));
    }

    // ---------- helpers ----------

    private String signupAndLogin(String email, String name) throws Exception {
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                                {"email":"%s","password":"password123","name":"%s"}
                                """.formatted(email, name)))
            .andExpect(status().isCreated());

        String response = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                                {"email":"%s","password":"password123"}
                                """.formatted(email)))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return JsonPath.read(response, "$.accessToken");
    }

    private long createStudy(String token, String name, int capacity) throws Exception {
        String response = mockMvc.perform(auth(post("/api/studies"), token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(studyJson(name, capacity)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return ((Number) JsonPath.read(response, "$.studyId")).longValue();
    }

    private ResultActions join(String token, long studyId) throws Exception {
        return mockMvc.perform(auth(post("/api/studies/{id}/members", studyId), token));
    }

    private ResultActions createSchedule(String token, long studyId) throws Exception {
        LocalDateTime start = LocalDateTime.now().plusDays(1).withNano(0);
        return mockMvc.perform(auth(post("/api/studies/{id}/schedules", studyId), token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                        {"title":"1주차","place":"강남","startAt":"%s","endAt":"%s"}
                        """.formatted(start, start.plusHours(2))));
    }

    private static String studyJson(String name, int capacity) {
        return """
                {"name":"%s","description":"설명","capacity":%d}
                """.formatted(name, capacity);
    }

    private static MockHttpServletRequestBuilder auth(MockHttpServletRequestBuilder builder, String token) {
        return builder.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
    }
}