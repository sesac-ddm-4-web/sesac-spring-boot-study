package com.example.personal_project.application;

import com.example.personal_project.domain.ShortLinkRepository;
import com.example.personal_project.domain.StudySession;
import com.example.personal_project.domain.StudySessionRepository;
import com.example.personal_project.global.exception.DuplicateShortenUrlCodeException;
import com.example.personal_project.global.exception.LackOfShortenUrlKeyException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// 테스트 트랜잭션을 사용하지 않고 Service의 실제 커밋·롤백 이후 상태를 확인한다.
@SpringBootTest
class StudySessionCreationServiceIntegrationTest {

    @Autowired private StudySessionCreationService service;
    @Autowired private StudySessionRegistrationService registration;
    @Autowired private StudySessionRepository sessions;
    @Autowired private ShortLinkRepository links;
    @Autowired private NamedParameterJdbcTemplate jdbc;

    private Long memberId;
    private int sessionNo;

    @BeforeEach
    void setUp() {
        assertFalse(TransactionSynchronizationManager.isActualTransactionActive());
        var keys = new GeneratedKeyHolder();
        jdbc.update("""
                INSERT INTO members (login_id, password_hash, display_name, role, created_at)
                VALUES (:loginId, 'unused-test-hash', '트랜잭션 테스트', 'ADMIN', UTC_TIMESTAMP(3))
                """, new MapSqlParameterSource("loginId", "tx-" + UUID.randomUUID()),
                keys, new String[]{"id"});
        assertNotNull(keys.getKey());
        memberId = keys.getKey().longValue();
        sessionNo = jdbc.queryForObject("SELECT COALESCE(MAX(session_no), 0) + 1 FROM study_sessions",
                Map.of(), Integer.class);
    }

    @AfterEach
    void cleanUpOwnCommittedFixtures() {
        if (memberId == null) {
            return;
        }
        var parameters = Map.of("memberId", memberId);
        jdbc.update("""
                DELETE FROM short_links WHERE session_id IN
                    (SELECT id FROM study_sessions WHERE created_by = :memberId)
                """, parameters);
        jdbc.update("DELETE FROM study_sessions WHERE created_by = :memberId", parameters);
        jdbc.update("DELETE FROM members WHERE id = :memberId", parameters);
    }

    @Test
    void successfulCreationCommitsSessionAndLink() {
        String code = newCode();

        Long id = service.create(session(sessionNo), code);

        assertFalse(TransactionSynchronizationManager.isActualTransactionActive());
        assertNotNull(sessions.findById(id));
        var link = links.findByCode(code);
        assertNotNull(link);
        assertEquals(id, link.sessionId());
        assertNull(link.materialId());
        assertEquals(0L, link.redirectCount());
        assertEquals(1L, ownSessionCount());
    }

    @Test
    void linkCodeCollisionRollsBackNewSessionAndPreservesExistingLink() {
        String code = newCode();
        Long originalId = service.create(session(sessionNo), code);
        links.incrementRedirectCount(code);
        var originalLink = links.findByCode(code);

        assertThrows(DuplicateKeyException.class,
                () -> service.create(session(sessionNo + 1), code));

        assertFalse(TransactionSynchronizationManager.isActualTransactionActive());
        assertEquals(0L, jdbc.queryForObject("""
                SELECT COUNT(*) FROM study_sessions
                WHERE created_by = :memberId AND session_no = :number
                """, Map.of("memberId", memberId, "number", sessionNo + 1), Long.class).longValue());
        assertEquals(1L, ownSessionCount());
        assertNotNull(sessions.findById(originalId));
        assertEquals(originalLink, links.findByCode(code));
    }

    @Test
    void sessionNumberCollisionDoesNotCreateAnotherLink() {
        String originalCode = newCode();
        Long id = service.create(session(sessionNo), originalCode);
        String rejectedCode = newCode();

        assertThrows(DuplicateKeyException.class,
                () -> service.create(session(sessionNo), rejectedCode));

        assertEquals(1L, ownSessionCount());
        assertNotNull(sessions.findById(id));
        assertNull(links.findByCode(rejectedCode));
        assertEquals(id, links.findByCode(originalCode).sessionId());
    }

    @Test
    void missingCodeDoesNotSaveSession() {
        assertThrows(IllegalArgumentException.class,
                () -> service.create(session(sessionNo), "   "));
        assertEquals(0L, ownSessionCount());
    }

    @Test
    void registrationCommitsNormalizedCustomCode() {
        String code = newCode();
        var result = registration.register(session(sessionNo), " " + code + " ");
        assertEquals(code, result.code());
        assertNotNull(sessions.findById(result.sessionId()));
        assertEquals(result.sessionId(), links.findByCode(code).sessionId());
    }

    @Test
    void registrationCustomConflictRollsBackAttempt() {
        String code = newCode();
        Long originalId = service.create(session(sessionNo), code);
        assertThrows(DuplicateShortenUrlCodeException.class,
                () -> registration.register(session(sessionNo + 1), code));
        assertEquals(1L, ownSessionCount());
        assertEquals(originalId, links.findByCode(code).sessionId());
    }

    @Test
    void automaticRetryRollsBackCollisionBeforeSuccessfulCommit() {
        String occupied = newCode();
        Long originalId = service.create(session(sessionNo), occupied);
        String available = newCode();
        ShortCodeGenerator generator = mock(ShortCodeGenerator.class);
        when(generator.generate()).thenReturn(occupied, available);
        // 실제 Spring 트랜잭션 프록시를 사용하는 생성 Service를 전달한다.
        var coordinator = new StudySessionRegistrationService(service, generator);

        var result = coordinator.register(session(sessionNo + 1), null);

        assertEquals(available, result.code());
        assertEquals(2L, ownSessionCount());
        assertNotNull(sessions.findById(result.sessionId()));
        assertEquals(result.sessionId(), links.findByCode(available).sessionId());
        assertEquals(originalId, links.findByCode(occupied).sessionId());
        verify(generator, times(2)).generate();
    }

    @Test
    void exhaustedAutomaticRetriesLeaveNoPartialSessions() {
        String occupied = newCode();
        Long originalId = service.create(session(sessionNo), occupied);
        ShortCodeGenerator generator = mock(ShortCodeGenerator.class);
        when(generator.generate()).thenReturn(occupied);
        var coordinator = new StudySessionRegistrationService(service, generator);

        assertThrows(LackOfShortenUrlKeyException.class,
                () -> coordinator.register(session(sessionNo + 1), null));

        verify(generator, times(5)).generate();
        assertEquals(1L, ownSessionCount());
        assertEquals(originalId, links.findByCode(occupied).sessionId());
    }

    private long ownSessionCount() {
        return jdbc.queryForObject("SELECT COUNT(*) FROM study_sessions WHERE created_by = :memberId",
                Map.of("memberId", memberId), Long.class);
    }

    private StudySession session(int number) {
        return new StudySession(null, number, "트랜잭션 검증", LocalDateTime.of(2026, 11, 20, 5, 0),
                "테스트 공지", "테스트 장소", null, null, memberId, null, null);
    }

    private String newCode() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
