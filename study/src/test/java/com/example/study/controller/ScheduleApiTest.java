package com.example.study.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import com.example.study.ApiTestSupport;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.ResultActions;

class ScheduleApiTest extends ApiTestSupport {

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    @Autowired
    Clock clock;

    @Test
    @DisplayName("기준 시간대는 Asia/Seoul이다")
    void seoulClock() {
        assertThat(clock.getZone()).isEqualTo(ZoneId.of("Asia/Seoul"));
    }

    @Test
    @DisplayName("스터디장이 등록하면 201과 일정을 반환한다. 스터디 요일과 달라도, 같은 일시여도 된다")
    void register() throws Exception {
        MockHttpSession owner = loginAs("owner");
        long studyId = createdStudyId(owner, studyBody("MONDAY", "19:00", "21:00"));

        // 2099-01-01은 목요일이다.
        for (int i = 0; i < 2; i++) {
            register(owner, studyId, body("2099-01-01T07:30"))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.meetingAt").value("2099-01-01T07:30"))
                    .andExpect(jsonPath("$.place").value("카페"))
                    .andExpect(jsonPath("$.content").value("1장 발표"));
        }
        assertThat(count("schedule")).isEqualTo(2);
        assertThat(jdbcTemplate.queryForList(
                "select date_format(meeting_at, '%Y-%m-%dT%H:%i') from schedule", String.class))
                .containsOnly("2099-01-01T07:30");
    }

    @Test
    @DisplayName("한국 시간으로 지난 일시는 400, 앞선 일시는 등록한다")
    void pastAndFutureInSeoul() throws Exception {
        MockHttpSession owner = loginAs("owner");
        long studyId = createdStudyId(owner, studyBody("MONDAY", "19:00", "21:00"));
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));

        register(owner, studyId, body(now.minusMinutes(1).format(FORMAT)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("현재보다 이후의 일시만 등록할 수 있습니다."));
        // UTC 기준으로는 미래여도 한국 시간으로 과거면 거절한다.
        register(owner, studyId, body(now.minusHours(1).format(FORMAT))).andExpect(status().isBadRequest());
        register(owner, studyId, body(now.plusMinutes(2).format(FORMAT))).andExpect(status().isCreated());
        assertThat(count("schedule")).isEqualTo(1);
    }

    @ParameterizedTest(name = "{0}={1}")
    @CsvSource(delimiter = '|', quoteCharacter = '`', textBlock = """
            meetingAt | null
            meetingAt | "2099-01-01"
            meetingAt | "2099-01-01 19:00"
            meetingAt | "2099-01-01T19:00:30"
            meetingAt | "2099-01-01T19:00Z"
            meetingAt | "2099-01-01T19:00+09:00"
            meetingAt | "2099-02-30T19:00"
            meetingAt | "2099-01-01T24:00"
            meetingAt | "+10000-01-01T19:00"
            place     | null
            place     | "  "
            content   | ""
            """)
    @DisplayName("입력값이 규칙에 맞지 않으면 400이다")
    void invalidInput(String field, String json) throws Exception {
        MockHttpSession owner = loginAs("owner");
        long studyId = createdStudyId(owner, studyBody("MONDAY", "19:00", "21:00"));
        var body = body("2099-01-01T19:00");
        body.put(field, objectMapper.readTree(json));

        register(owner, studyId, body).andExpect(status().isBadRequest());
        assertThat(count("schedule")).isZero();
    }

    @Test
    @DisplayName("장소 100자, 내용 1000자까지 허용한다")
    void lengthLimits() throws Exception {
        MockHttpSession owner = loginAs("owner");
        long studyId = createdStudyId(owner, studyBody("MONDAY", "19:00", "21:00"));
        var body = body("2099-01-01T19:00");
        body.put("place", "장".repeat(100));
        body.put("content", "내".repeat(1000));
        register(owner, studyId, body).andExpect(status().isCreated());

        body.put("place", "장".repeat(101));
        register(owner, studyId, body).andExpect(status().isBadRequest());
        body.put("place", "장");
        body.put("content", "내".repeat(1001));
        register(owner, studyId, body).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("등록은 스터디장만 할 수 있고, 없는 스터디는 404이다")
    void onlyOwnerRegisters() throws Exception {
        long studyId = createdStudyId(loginAs("owner"), studyBody("MONDAY", "19:00", "21:00"));
        MockHttpSession member = loginAs("member");
        join(member, studyId);

        for (MockHttpSession session : new MockHttpSession[] { member, loginAs("stranger") }) {
            register(session, studyId, body("2099-01-01T19:00"))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.message").value("스터디장만 할 수 있습니다."));
        }
        register(member, 999999L, body("2099-01-01T19:00")).andExpect(status().isNotFound());
        assertThat(count("schedule")).isZero();
    }

    @Test
    @DisplayName("목록은 해당 스터디의 지난 일정까지 일시 오름차순이다")
    void list() throws Exception {
        MockHttpSession owner = loginAs("owner");
        long studyId = createdStudyId(owner, studyBody("MONDAY", "19:00", "21:00"));
        long otherId = createdStudyId(owner, studyBody("TUESDAY", "19:00", "21:00"));
        list(owner, studyId).andExpect(status().isOk()).andExpect(jsonPath("$").isEmpty());
        register(owner, studyId, body("2099-03-01T19:00"));
        register(owner, studyId, body("2099-01-01T19:00"));
        register(owner, otherId, body("2099-02-01T19:00"));
        // 지난 일정은 API로 만들 수 없으므로 직접 넣는다.
        jdbcTemplate.update("insert into schedule (study_id, meeting_at, place, content) "
                + "values (?, '2020-01-01 19:00', '도서관', '지난 모임')", studyId);

        list(owner, studyId)
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].meetingAt").value("2020-01-01T19:00"))
                .andExpect(jsonPath("$[1].meetingAt").value("2099-01-01T19:00"))
                .andExpect(jsonPath("$[2].meetingAt").value("2099-03-01T19:00"));
    }

    @Test
    @DisplayName("조회는 스터디장과 현재 참여자만 가능하다. 탈퇴하면 못 보고 재가입하면 다시 본다")
    void readPermission() throws Exception {
        MockHttpSession owner = loginAs("owner");
        long studyId = createdStudyId(owner, studyBody("MONDAY", "19:00", "21:00"));
        register(owner, studyId, body("2099-01-01T19:00"));
        MockHttpSession member = loginAs("member");

        expectDenied(list(member, studyId));
        join(member, studyId);
        list(member, studyId).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
        mockMvc.perform(delete("/api/studies/" + studyId + "/members/me").session(member))
                .andExpect(status().isNoContent());
        expectDenied(list(member, studyId));
        join(member, studyId);
        list(member, studyId).andExpect(status().isOk());
        list(member, 999999L).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("스터디를 삭제하면 일정도 삭제된다")
    void deletedWithStudy() throws Exception {
        MockHttpSession owner = loginAs("owner");
        long studyId = createdStudyId(owner, studyBody("MONDAY", "19:00", "21:00"));
        register(owner, studyId, body("2099-01-01T19:00"));
        register(owner, studyId, body("2099-01-08T19:00"));

        mockMvc.perform(delete("/api/studies/" + studyId).session(owner)).andExpect(status().isNoContent());

        assertThat(count("schedule")).isZero();
        list(owner, studyId).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("일정 API는 로그인이 필요하다")
    void requiresLogin() throws Exception {
        mockMvc.perform(get("/api/studies/1/schedules")).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/studies/1/schedules")).andExpect(status().isUnauthorized());
    }

    private Map<String, Object> body(String meetingAt) {
        return new HashMap<>(Map.of("meetingAt", meetingAt, "place", "카페", "content", "1장 발표"));
    }

    private ResultActions register(MockHttpSession session, long studyId, Object body) throws Exception {
        return send(post("/api/studies/" + studyId + "/schedules").session(session), body);
    }

    private ResultActions list(MockHttpSession session, long studyId) throws Exception {
        return mockMvc.perform(get("/api/studies/" + studyId + "/schedules").session(session));
    }

    private void join(MockHttpSession session, long studyId) throws Exception {
        mockMvc.perform(post("/api/studies/" + studyId + "/members").session(session))
                .andExpect(status().isCreated());
    }

    private void expectDenied(ResultActions result) throws Exception {
        result.andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("스터디장과 참여자만 일정을 볼 수 있습니다."));
    }
}
