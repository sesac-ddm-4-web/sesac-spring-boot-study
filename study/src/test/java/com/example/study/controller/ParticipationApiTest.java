package com.example.study.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import com.example.study.ApiTestSupport;
import com.example.study.service.StudyDeletionService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.ResultActions;

class ParticipationApiTest extends ApiTestSupport {

    @Autowired
    StudyDeletionService studyDeletionService;

    @Test
    @DisplayName("참여하면 201과 상세를 반환하고 인원이 늘어난다")
    void join() throws Exception {
        long studyId = createdStudyId(loginAs("owner"), studyBody("MONDAY", "19:00", "21:00"));
        MockHttpSession member = loginAs("member");

        join(member, studyId)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(studyId))
                .andExpect(jsonPath("$.memberCount").value(2))
                .andExpect(jsonPath("$.myRole").value("MEMBER"));
        mockMvc.perform(get("/api/studies/" + studyId).session(member))
                .andExpect(jsonPath("$.memberCount").value(2))
                .andExpect(jsonPath("$.myRole").value("MEMBER"));
    }

    @Test
    @DisplayName("없는 스터디는 404, 스터디장과 참여자의 재신청은 중복 참여 409이다")
    void notFoundAndDuplicate() throws Exception {
        MockHttpSession owner = loginAs("owner");
        long studyId = createdStudyId(owner, studyBody("MONDAY", "19:00", "21:00"));
        MockHttpSession member = loginAs("member");
        join(member, studyId).andExpect(status().isCreated());

        join(member, 999999L).andExpect(status().isNotFound());
        expectError(join(owner, studyId), 409, "이미 참여 중인 스터디입니다.");
        expectError(join(member, studyId), 409, "이미 참여 중인 스터디입니다.");
        assertThat(count("study_member")).isEqualTo(2);
    }

    @Test
    @DisplayName("검사 순서: 중복 참여 → 모집 상태 → 정원 → 시간 겹침")
    void checkOrder() throws Exception {
        MockHttpSession owner = loginAs("owner");
        var body = studyBody("MONDAY", "19:00", "21:00");
        body.put("capacity", 2);
        long studyId = createdStudyId(owner, body);
        MockHttpSession member = loginAs("member");
        join(member, studyId).andExpect(status().isCreated());
        // 신청자는 정원이 찬 마감 스터디와 시간까지 겹친다.
        MockHttpSession late = loginAs("late");
        createdStudyId(late, studyBody("MONDAY", "20:00", "22:00"));
        changeStatus(owner, studyId, "CLOSED");

        expectError(join(member, studyId), 409, "이미 참여 중인 스터디입니다.");
        expectError(join(late, studyId), 409, "모집이 마감된 스터디입니다.");
        changeStatus(owner, studyId, "RECRUITING");
        expectError(join(late, studyId), 409, "정원이 가득 찼습니다.");
        mockMvc.perform(delete("/api/studies/" + studyId + "/members/me").session(member))
                .andExpect(status().isNoContent());
        expectError(join(late, studyId), 409, "기존 스터디 시간과 겹칩니다.");
    }

    @Test
    @DisplayName("정원이 차도 모집 상태는 바뀌지 않는다")
    void statusUnchangedWhenFull() throws Exception {
        var body = studyBody("MONDAY", "19:00", "21:00");
        body.put("capacity", 2);
        long studyId = createdStudyId(loginAs("owner"), body);

        join(loginAs("member"), studyId)
                .andExpect(jsonPath("$.memberCount").value(2))
                .andExpect(jsonPath("$.recruitStatus").value("RECRUITING"));
    }

    @Test
    @DisplayName("참여 중인 스터디와도 겹침을 검사하며, 맞닿는 시간은 참여·개설할 수 있다")
    void timeOverlap() throws Exception {
        long base = createdStudyId(loginAs("a"), studyBody("MONDAY", "19:00", "21:00"));
        long overlapping = createdStudyId(loginAs("b"), studyBody("MONDAY", "20:00", "22:00"));
        long adjacent = createdStudyId(loginAs("c"), studyBody("MONDAY", "21:00", "23:00"));
        MockHttpSession member = loginAs("member");
        join(member, base).andExpect(status().isCreated());

        expectError(join(member, overlapping), 409, "기존 스터디 시간과 겹칩니다.");
        expectError(createStudy(member, studyBody("MONDAY", "18:00", "19:30")), 409, "기존 스터디 시간과 겹칩니다.");
        join(member, adjacent).andExpect(status().isCreated());
        createStudy(member, studyBody("MONDAY", "17:00", "19:00")).andExpect(status().isCreated());
    }

    @Test
    @DisplayName("탈퇴하면 204이고 참여 정보를 지우며 LEFT 이력을 남긴다")
    void leave() throws Exception {
        long studyId = createdStudyId(loginAs("owner"), studyBody("MONDAY", "19:00", "21:00"));
        MockHttpSession member = loginAs("member");
        join(member, studyId);
        LocalDateTime before = nowInSeoul();

        leave(member, studyId).andExpect(status().isNoContent());

        assertThat(count("study_member")).isEqualTo(1);
        var me = objectMapper.readTree(mockMvc.perform(get("/api/members/me").session(member))
                .andExpect(jsonPath("$.joinedStudies").isEmpty())
                .andExpect(jsonPath("$.histories.length()").value(1))
                .andExpect(jsonPath("$.histories[0].studyId").value(studyId))
                .andExpect(jsonPath("$.histories[0].studyName").value("알고리즘"))
                .andExpect(jsonPath("$.histories[0].reason").value("LEFT"))
                .andReturn().getResponse().getContentAsString());
        String endedAt = me.get("histories").get(0).get("endedAt").asString();
        assertThat(endedAt).matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}");
        assertThat(LocalDateTime.parse(endedAt)).isBetween(before, nowInSeoul());
    }

    @Test
    @DisplayName("스터디장의 탈퇴는 403, 미참여·없는 스터디의 탈퇴는 404이며 이력이 생기지 않는다")
    void invalidLeave() throws Exception {
        MockHttpSession owner = loginAs("owner");
        long studyId = createdStudyId(owner, studyBody("MONDAY", "19:00", "21:00"));
        MockHttpSession stranger = loginAs("stranger");

        expectError(leave(owner, studyId), 403, "스터디장은 탈퇴할 수 없습니다. 스터디를 삭제해 주세요.");
        expectError(leave(stranger, studyId), 404, "참여 중인 스터디가 아닙니다.");
        expectError(leave(stranger, 999999L), 404, "스터디를 찾을 수 없습니다.");
        assertThat(count("study_history")).isZero();
        assertThat(count("study_member")).isEqualTo(1);
    }

    @Test
    @DisplayName("재가입은 참여와 같은 조건이며, 다시 탈퇴하면 이력이 한 건 더 쌓인다")
    void rejoin() throws Exception {
        MockHttpSession owner = loginAs("owner");
        long studyId = createdStudyId(owner, studyBody("MONDAY", "19:00", "21:00"));
        MockHttpSession member = loginAs("member");
        join(member, studyId);
        leave(member, studyId);

        changeStatus(owner, studyId, "CLOSED");
        expectError(join(member, studyId), 409, "모집이 마감된 스터디입니다.");
        changeStatus(owner, studyId, "RECRUITING");
        join(member, studyId).andExpect(status().isCreated());
        leave(member, studyId).andExpect(status().isNoContent());

        mockMvc.perform(get("/api/members/me").session(member))
                .andExpect(jsonPath("$.histories.length()").value(2))
                .andExpect(jsonPath("$.histories[0].reason").value("LEFT"))
                .andExpect(jsonPath("$.histories[1].reason").value("LEFT"));
    }

    @Test
    @DisplayName("재가입도 정원과 시간 겹침을 다시 검사한다")
    void rejoinChecksCapacityAndOverlap() throws Exception {
        var body = studyBody("MONDAY", "19:00", "21:00");
        body.put("capacity", 2);
        long studyId = createdStudyId(loginAs("owner"), body);
        MockHttpSession member = loginAs("member");
        join(member, studyId);
        leave(member, studyId);

        MockHttpSession other = loginAs("other");
        join(other, studyId).andExpect(status().isCreated());
        expectError(join(member, studyId), 409, "정원이 가득 찼습니다.");
        leave(other, studyId);
        createStudy(member, studyBody("MONDAY", "20:00", "22:00")).andExpect(status().isCreated());
        expectError(join(member, studyId), 409, "기존 스터디 시간과 겹칩니다.");
        assertThat(reasonsOf("member")).containsExactly("LEFT");
    }

    @Test
    @DisplayName("삭제하면 스터디장과 현재 참여자에게만 삭제 이력이 남고, 이력은 최신순이다")
    void historiesAfterDelete() throws Exception {
        MockHttpSession owner = loginAs("owner");
        long studyId = createdStudyId(owner, studyBody("MONDAY", "19:00", "21:00"));
        MockHttpSession stayed = loginAs("stayed");
        MockHttpSession left = loginAs("left");
        MockHttpSession rejoined = loginAs("rejoined");
        join(stayed, studyId);
        join(left, studyId);
        leave(left, studyId);
        join(rejoined, studyId);
        leave(rejoined, studyId);
        join(rejoined, studyId);

        mockMvc.perform(delete("/api/studies/" + studyId).session(owner)).andExpect(status().isNoContent());

        assertThat(count("study")).isZero();
        assertThat(count("study_member")).isZero();
        assertThat(reasonsOf("owner")).containsExactly("OWNER_DELETED");
        assertThat(reasonsOf("stayed")).containsExactly("STUDY_DELETED");
        assertThat(reasonsOf("left")).containsExactly("LEFT");
        assertThat(reasonsOf("rejoined")).containsExactlyInAnyOrder("LEFT", "STUDY_DELETED");
        // 삭제된 스터디의 이름이 이력에 남고, 최신 이력이 앞에 온다.
        mockMvc.perform(get("/api/members/me").session(rejoined))
                .andExpect(jsonPath("$.histories[0].reason").value("STUDY_DELETED"))
                .andExpect(jsonPath("$.histories[0].studyName").value("알고리즘"))
                .andExpect(jsonPath("$.histories[1].reason").value("LEFT"));
    }

    @Test
    @DisplayName("삭제 도중 실패하면 이력과 참여 정보까지 모두 되돌린다")
    void deletionIsAtomic() throws Exception {
        MockHttpSession owner = loginAs("owner");
        long studyId = createdStudyId(owner, studyBody("MONDAY", "19:00", "21:00"));
        join(loginAs("member"), studyId);
        Long ownerId = jdbcTemplate.queryForObject("select owner_id from study where id = ?", Long.class, studyId);
        jdbcTemplate.update("insert into schedule (study_id, meeting_at, place, content) "
                + "values (?, '2030-01-01 19:00', '카페', '모임')", studyId);
        // 마지막 단계인 스터디 삭제만 실패하도록, 스터디를 참조하는 임시 테이블을 만든다.
        jdbcTemplate.execute("create table delete_blocker (study_id bigint not null, "
                + "foreign key (study_id) references study (id))");
        try {
            jdbcTemplate.update("insert into delete_blocker values (?)", studyId);

            assertThatThrownBy(() -> studyDeletionService.delete(ownerId, studyId))
                    .isInstanceOf(DataIntegrityViolationException.class);

            assertThat(count("study")).isEqualTo(1);
            assertThat(count("study_member")).isEqualTo(2);
            assertThat(count("schedule")).isEqualTo(1);
            assertThat(count("study_history")).isZero();
        } finally {
            jdbcTemplate.execute("drop table delete_blocker");
        }
    }

    @Test
    @DisplayName("참여 목록에는 본인이 개설한 스터디를 넣지 않고, 현재 목록에는 시각이 없다")
    void myStudies() throws Exception {
        long joined = createdStudyId(loginAs("owner"), studyBody("MONDAY", "19:00", "21:00"));
        MockHttpSession member = loginAs("member");
        long owned = createdStudyId(member, studyBody("TUESDAY", "19:00", "21:00"));
        join(member, joined);

        for (String url : new String[] { "/api/members/me", "/api/members/me/studies" }) {
            mockMvc.perform(get(url).session(member))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.ownedStudies.length()").value(1))
                    .andExpect(jsonPath("$.ownedStudies[0].id").value(owned))
                    .andExpect(jsonPath("$.joinedStudies.length()").value(1))
                    .andExpect(jsonPath("$.joinedStudies[0].id").value(joined))
                    .andExpect(jsonPath("$.joinedStudies[0].ownerName").value("owner"))
                    // 인원은 본인만이 아니라 전체를 센다.
                    .andExpect(jsonPath("$.joinedStudies[0].memberCount").value(2))
                    .andExpect(jsonPath("$.joinedStudies[0].endedAt").doesNotExist())
                    .andExpect(jsonPath("$.joinedStudies[0].joinedAt").doesNotExist());
        }
        mockMvc.perform(get("/api/members/me/studies").session(member))
                .andExpect(jsonPath("$.histories").doesNotExist());
    }

    @Test
    @DisplayName("참여와 탈퇴는 로그인이 필요하다")
    void requiresLogin() throws Exception {
        mockMvc.perform(post("/api/studies/1/members")).andExpect(status().isUnauthorized());
        mockMvc.perform(delete("/api/studies/1/members/me")).andExpect(status().isUnauthorized());
    }

    private ResultActions join(MockHttpSession session, long studyId) throws Exception {
        return mockMvc.perform(post("/api/studies/" + studyId + "/members").session(session));
    }

    private ResultActions leave(MockHttpSession session, long studyId) throws Exception {
        return mockMvc.perform(delete("/api/studies/" + studyId + "/members/me").session(session));
    }

    private void changeStatus(MockHttpSession owner, long studyId, String value) throws Exception {
        send(patch("/api/studies/" + studyId + "/recruit-status").session(owner), Map.of("recruitStatus", value))
                .andExpect(status().isOk());
    }

    private void expectError(ResultActions result, int code, String message) throws Exception {
        result.andExpect(status().is(code)).andExpect(jsonPath("$.message").value(message));
    }

    private List<String> reasonsOf(String name) {
        return jdbcTemplate.queryForList(
                "select h.reason from study_history h join member m on m.id = h.member_id where m.name = ?",
                String.class, name);
    }

    private LocalDateTime nowInSeoul() {
        return LocalDateTime.now(ZoneId.of("Asia/Seoul")).truncatedTo(ChronoUnit.SECONDS);
    }
}
