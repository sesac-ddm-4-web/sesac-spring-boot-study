package com.example.personal_project.infrastructure.repository;

import com.example.personal_project.domain.StudySession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class JdbcStudySessionRepositoryTest {

    private static final LocalDateTime STARTS_AT =
            LocalDateTime.of(2026, 11, 20, 5, 0, 0, 123_000_000);

    @Autowired
    private JdbcStudySessionRepository repository;

    @Autowired
    private NamedParameterJdbcTemplate jdbc;

    private Long memberId;
    private int sessionNo;

    @BeforeEach
    void setUp() {
        var keyHolder = new GeneratedKeyHolder();
        jdbc.update("""
                INSERT INTO members
                    (login_id, password_hash, display_name, role, created_at)
                VALUES
                    (:loginId, 'unused-test-hash', '회차 테스트 운영자',
                     'ADMIN', UTC_TIMESTAMP(3))
                """, new MapSqlParameterSource("loginId", "session-" + UUID.randomUUID()),
                keyHolder, new String[]{"id"});
        assertNotNull(keyHolder.getKey());
        memberId = keyHolder.getKey().longValue();
        sessionNo = jdbc.queryForObject("""
                SELECT COALESCE(MAX(session_no), 0) + 1 FROM study_sessions
                """, Map.of(), Integer.class);
    }

    @Test
    void insertReturnsGeneratedIdAndRestoresEveryField() {
        StudySession input = session(sessionNo, memberId, "스터디룸 A", null, "노트북");

        Long id = repository.insert(input);
        StudySession saved = repository.findById(id);

        assertNotNull(id);
        assertTrue(id > 0);
        assertNull(input.id());
        assertNotNull(saved);
        assertEquals(id, saved.id());
        assertEquals(sessionNo, saved.sessionNo());
        assertEquals("JPA 스터디", saved.title());
        assertEquals(STARTS_AT, saved.startsAt());
        assertEquals("예제를 실행하고 참여해주세요.", saved.notice());
        assertEquals("스터디룸 A", saved.location());
        assertNull(saved.meetingUrl());
        assertEquals("노트북", saved.preparation());
        assertEquals(memberId, saved.createdBy());
        assertNotNull(saved.createdAt());
        assertNotNull(saved.updatedAt());
    }

    @Test
    void onlineMeetingUrlAndNullableFieldsAreRestored() {
        Long id = repository.insert(session(sessionNo, memberId, null,
                "https://example.com/meeting", null));

        StudySession saved = repository.findById(id);

        assertNull(saved.location());
        assertEquals("https://example.com/meeting", saved.meetingUrl());
        assertNull(saved.preparation());
    }

    @Test
    void missingIdReturnsNull() {
        Long missingId = jdbc.queryForObject(
                "SELECT COALESCE(MAX(id), 0) + 1 FROM study_sessions", Map.of(), Long.class);
        assertNull(repository.findById(missingId));
    }

    @Test
    void duplicateSessionNumberIsRejectedWithoutChangingExistingSession() {
        Long id = repository.insert(session(sessionNo, memberId, "원래 장소", null, null));
        StudySession before = repository.findById(id);

        assertThrows(DuplicateKeyException.class,
                () -> repository.insert(session(sessionNo, memberId, "변경 시도", null, null)));

        assertEquals(before, repository.findById(id));
        assertEquals(1L, jdbc.queryForObject(
                "SELECT COUNT(*) FROM study_sessions WHERE session_no = :number",
                Map.of("number", sessionNo), Long.class).longValue());
    }

    @Test
    void nonexistentCreatorIsRejectedWithoutSavingSession() {
        Long missingMemberId = jdbc.queryForObject(
                "SELECT COALESCE(MAX(id), 0) + 1 FROM members", Map.of(), Long.class);

        assertThrows(DataIntegrityViolationException.class,
                () -> repository.insert(session(sessionNo, missingMemberId, "장소", null, null)));

        assertEquals(0L, jdbc.queryForObject(
                "SELECT COUNT(*) FROM study_sessions WHERE session_no = :number",
                Map.of("number", sessionNo), Long.class).longValue());
    }

    private StudySession session(int number, Long creatorId, String location,
                                 String meetingUrl, String preparation) {
        return new StudySession(null, number, "JPA 스터디", STARTS_AT,
                "예제를 실행하고 참여해주세요.", location, meetingUrl, preparation,
                creatorId, null, null);
    }
}
