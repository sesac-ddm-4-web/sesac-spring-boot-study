package com.example.study.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import com.example.study.ApiTestSupport;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.RequestBuilder;

class StudyApiTest extends ApiTestSupport {

    @Test
    @DisplayName("개설하면 201과 상세를 반환하고 스터디장을 참여 정보에 저장한다")
    void open() throws Exception {
        MockHttpSession owner = loginAs("owner");

        createStudy(owner, studyBody("MONDAY", "19:00", "21:00"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("알고리즘"))
                .andExpect(jsonPath("$.description").value("매주 온라인으로 문제 풀이"))
                .andExpect(jsonPath("$.ownerName").value("owner"))
                .andExpect(jsonPath("$.capacity").value(4))
                .andExpect(jsonPath("$.memberCount").value(1))
                .andExpect(jsonPath("$.recruitStatus").value("RECRUITING"))
                .andExpect(jsonPath("$.dayOfWeek").value("MONDAY"))
                .andExpect(jsonPath("$.startTime").value("19:00"))
                .andExpect(jsonPath("$.endTime").value("21:00"))
                .andExpect(jsonPath("$.myRole").value("OWNER"));
        assertThat(count("study_member")).isEqualTo(1);
    }

    @ParameterizedTest(name = "{0}={1}")
    @CsvSource(delimiter = '|', quoteCharacter = '`', textBlock = """
            name          | "  "
            name          | "123456789012345678901234567890123456789012345678901"
            description   | ""
            capacity      | 1
            capacity      | 101
            capacity      | 2.5
            capacity      | null
            recruitStatus | "OPEN"
            recruitStatus | 0
            dayOfWeek     | 1
            dayOfWeek     | "MON"
            startTime     | "24:00"
            startTime     | "19:00:30"
            startTime     | "9시"
            endTime       | null
            """)
    @DisplayName("개설 입력값이 규칙에 맞지 않으면 400이고 저장하지 않는다")
    void invalidOpen(String field, String json) throws Exception {
        MockHttpSession owner = loginAs("owner");
        var body = studyBody("MONDAY", "19:00", "21:00");
        body.put(field, objectMapper.readTree(json));

        createStudy(owner, body).andExpect(status().isBadRequest());
        assertThat(count("study")).isZero();
    }

    @Test
    @DisplayName("정원 2명과 100명은 허용한다")
    void capacityBounds() throws Exception {
        MockHttpSession owner = loginAs("owner");
        var two = studyBody("MONDAY", "19:00", "21:00");
        two.put("capacity", 2);
        var hundred = studyBody("TUESDAY", "19:00", "21:00");
        hundred.put("capacity", 100);

        createStudy(owner, two).andExpect(status().isCreated());
        createStudy(owner, hundred).andExpect(status().isCreated());
    }

    @ParameterizedTest
    @CsvSource({ "21:00, 19:00", "19:00, 19:00" })
    @DisplayName("시작이 종료보다 빠르지 않으면 400이다")
    void invalidTimeOrder(String start, String end) throws Exception {
        createStudy(loginAs("owner"), studyBody("MONDAY", start, end))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("시작 시각은 종료 시각보다 빨라야 합니다."));
    }

    @Test
    @DisplayName("본인 스터디와 겹치면 409, 맞닿거나 다른 요일이면 허용, 스터디명 중복은 허용한다")
    void timeOverlapOnOpen() throws Exception {
        MockHttpSession owner = loginAs("owner");
        createStudy(owner, studyBody("MONDAY", "19:00", "21:00")).andExpect(status().isCreated());

        createStudy(owner, studyBody("MONDAY", "20:00", "22:00"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("기존 스터디 시간과 겹칩니다."));
        createStudy(owner, studyBody("MONDAY", "21:00", "23:00")).andExpect(status().isCreated());
        createStudy(owner, studyBody("MONDAY", "17:00", "19:00")).andExpect(status().isCreated());
        createStudy(owner, studyBody("TUESDAY", "19:00", "21:00")).andExpect(status().isCreated());
        // 다른 회원의 시간과는 비교하지 않는다.
        createStudy(loginAs("other"), studyBody("MONDAY", "19:00", "21:00")).andExpect(status().isCreated());
    }

    @Test
    @DisplayName("목록은 마감 스터디를 포함해 최신순이고, 상세는 보는 사람의 역할을 알려준다")
    void listAndDetail() throws Exception {
        MockHttpSession owner = loginAs("owner");
        MockHttpSession other = loginAs("other");
        long first = createdStudyId(owner, studyBody("MONDAY", "19:00", "21:00"));
        var closed = studyBody("TUESDAY", "19:00", "21:00");
        closed.put("recruitStatus", "CLOSED");
        long second = createdStudyId(owner, closed);

        mockMvc.perform(get("/api/studies").session(other))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(second))
                .andExpect(jsonPath("$[0].recruitStatus").value("CLOSED"))
                .andExpect(jsonPath("$[1].id").value(first))
                .andExpect(jsonPath("$[1].ownerName").value("owner"))
                .andExpect(jsonPath("$[1].memberCount").value(1))
                .andExpect(jsonPath("$[1].startTime").value("19:00"))
                .andExpect(jsonPath("$[1].description").doesNotExist());
        mockMvc.perform(get("/api/studies/" + first).session(other))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.myRole").value("NONE"));
        mockMvc.perform(get("/api/studies/" + first).session(owner))
                .andExpect(jsonPath("$.myRole").value("OWNER"));
        mockMvc.perform(get("/api/studies/999999").session(owner))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("스터디를 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("내 개설 목록은 본인이 만든 스터디만 보여준다")
    void myOwnedStudies() throws Exception {
        MockHttpSession owner = loginAs("owner");
        long mine = createdStudyId(owner, studyBody("MONDAY", "19:00", "21:00"));
        createStudy(loginAs("other"), studyBody("MONDAY", "19:00", "21:00"));

        for (String url : new String[] { "/api/members/me", "/api/members/me/studies" }) {
            mockMvc.perform(get(url).session(owner))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.ownedStudies.length()").value(1))
                    .andExpect(jsonPath("$.ownedStudies[0].id").value(mine));
        }
    }

    @Test
    @DisplayName("모집 상태는 스터디장만 바꾸며 같은 값으로도 성공한다")
    void changeRecruitStatus() throws Exception {
        MockHttpSession owner = loginAs("owner");
        long id = createdStudyId(owner, studyBody("MONDAY", "19:00", "21:00"));
        String url = "/api/studies/" + id + "/recruit-status";

        for (int i = 0; i < 2; i++) {
            send(patch(url).session(owner), Map.of("recruitStatus", "CLOSED"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.recruitStatus").value("CLOSED"));
        }
        send(patch(url).session(loginAs("other")), Map.of("recruitStatus", "RECRUITING"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("스터디장만 할 수 있습니다."));
        mockMvc.perform(patch(url).session(owner).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/studies/" + id).session(owner))
                .andExpect(jsonPath("$.recruitStatus").value("CLOSED"));
        send(patch("/api/studies/999999/recruit-status").session(owner), Map.of("recruitStatus", "CLOSED"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("삭제하면 스터디와 참여 정보가 사라지고 스터디장의 OWNER_DELETED 이력이 남는다")
    void deleteStudy() throws Exception {
        MockHttpSession owner = loginAs("owner");
        long id = createdStudyId(owner, studyBody("MONDAY", "19:00", "21:00"));

        mockMvc.perform(delete("/api/studies/" + id).session(loginAs("other")))
                .andExpect(status().isForbidden());
        assertThat(count("study")).isEqualTo(1);
        assertThat(count("study_history")).isZero();

        mockMvc.perform(delete("/api/studies/" + id).session(owner)).andExpect(status().isNoContent());

        assertThat(count("study")).isZero();
        assertThat(count("study_member")).isZero();
        var history = jdbcTemplate.queryForMap("select study_id, study_name, reason, ended_at from study_history");
        assertThat(history.get("study_id")).isEqualTo(id);
        assertThat(history.get("study_name")).isEqualTo("알고리즘");
        assertThat(history.get("reason").toString()).isEqualTo("OWNER_DELETED");
        assertThat(history.get("ended_at")).isNotNull();
        mockMvc.perform(delete("/api/studies/" + id).session(owner)).andExpect(status().isNotFound());
        // 삭제한 시간대에는 다시 개설할 수 있다.
        createStudy(owner, studyBody("MONDAY", "19:00", "21:00")).andExpect(status().isCreated());
    }

    @Test
    @DisplayName("스터디 API는 로그인이 필요하다")
    void requiresLogin() throws Exception {
        for (RequestBuilder request : new RequestBuilder[] {
                get("/api/studies"), get("/api/studies/1"), delete("/api/studies/1"),
                get("/api/members/me/studies") }) {
            mockMvc.perform(request).andExpect(status().isUnauthorized());
        }
    }
}
