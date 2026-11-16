package com.example.personal_project.infrastructure.repository;

import com.example.personal_project.domain.ShortLink;
import com.example.personal_project.global.exception.ShortLinkCodeConflictException;
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

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class JdbcShortLinkRepositoryTest {

    @Autowired
    private JdbcShortLinkRepository repository;

    @Autowired
    private NamedParameterJdbcTemplate jdbc;

    private Long sessionId;

    @BeforeEach
    void setUp() {
        Long memberId = insertAndReturnId("""
                INSERT INTO members
                    (login_id, password_hash, display_name, role, created_at)
                VALUES
                    (:loginId, 'unused-test-hash', '테스트 운영자',
                     'ADMIN', UTC_TIMESTAMP(3))
                """, new MapSqlParameterSource("loginId", "test-" + UUID.randomUUID()));

        Integer sessionNo = jdbc.queryForObject("""
                SELECT COALESCE(MAX(session_no), 0) + 1
                FROM study_sessions
                """, Map.of(), Integer.class);

        sessionId = insertAndReturnId("""
                INSERT INTO study_sessions
                    (session_no, title, starts_at, notice, location,
                     created_by, created_at, updated_at)
                VALUES
                    (:sessionNo, '테스트 회차', UTC_TIMESTAMP(3),
                     '테스트 공지', '테스트 장소',
                     :createdBy, UTC_TIMESTAMP(3), UTC_TIMESTAMP(3))
                """, new MapSqlParameterSource()
                .addValue("sessionNo", sessionNo)
                .addValue("createdBy", memberId));
    }

    @Test
    void sessionLinkCanBeSavedAndRestored() {
        String code = newCode();
        repository.insert(new ShortLink(null, code, sessionId, null, 0));

        ShortLink saved = repository.findByCode(code);

        assertNotNull(saved);
        assertNotNull(saved.id());
        assertEquals(code, saved.code());
        assertEquals(sessionId, saved.sessionId());
        assertNull(saved.materialId());
        assertEquals(0L, saved.redirectCount());
    }

    @Test
    void redirectCountIsPersisted() {
        String code = newCode();
        repository.insert(new ShortLink(null, code, sessionId, null, 0));

        assertEquals(1, repository.incrementRedirectCount(code));
        assertEquals(1, repository.incrementRedirectCount(code));

        ShortLink saved = repository.findByCode(code);
        assertEquals(2L, saved.redirectCount());
        assertEquals(sessionId, saved.sessionId());
    }

    @Test
    void missingCodeReturnsNullAndUpdatesNoRows() {
        String code = newCode();

        assertNull(repository.findByCode(code));
        assertEquals(0, repository.incrementRedirectCount(code));
        assertNull(repository.findByCode(code));
    }

    @Test
    void materialLinkCanBeSavedAndRestored() {
        Long materialId = createMaterial();
        String code = newCode();
        repository.insert(new ShortLink(null, code, null, materialId, 0));

        ShortLink saved = repository.findByCode(code);
        assertNotNull(saved);
        assertNotNull(saved.id());
        assertEquals(code, saved.code());
        assertNull(saved.sessionId());
        assertEquals(materialId, saved.materialId());
        assertEquals(0L, saved.redirectCount());
    }

    @Test
    void duplicateCodeIsRejectedAcrossDifferentTargets() {
        Long materialId = createMaterial();
        String code = newCode();
        repository.insert(new ShortLink(null, code, sessionId, null, 0));
        repository.incrementRedirectCount(code);

        assertThrows(ShortLinkCodeConflictException.class,
                () -> repository.insert(new ShortLink(null, code, null, materialId, 0)));

        ShortLink existing = repository.findByCode(code);
        assertEquals(sessionId, existing.sessionId());
        assertNull(existing.materialId());
        assertEquals(1L, existing.redirectCount());
    }

    @Test
    void codesAreCaseSensitive() {
        Long materialId = createMaterial();
        String suffix = newCode();
        String upperCode = "A" + suffix;
        String lowerCode = "a" + suffix;
        repository.insert(new ShortLink(null, upperCode, sessionId, null, 0));
        repository.insert(new ShortLink(null, lowerCode, null, materialId, 0));

        ShortLink upper = repository.findByCode(upperCode);
        ShortLink lower = repository.findByCode(lowerCode);
        assertNotNull(upper);
        assertNotNull(lower);
        assertNotEquals(upper.id(), lower.id());
        assertEquals(sessionId, upper.sessionId());
        assertNull(upper.materialId());
        assertNull(lower.sessionId());
        assertEquals(materialId, lower.materialId());
    }

    @Test
    void sameSessionCannotHaveTwoShortLinks() {
        String firstCode = newCode();
        String secondCode = newCode();
        repository.insert(new ShortLink(null, firstCode, sessionId, null, 0));

        DuplicateKeyException failure = assertThrows(DuplicateKeyException.class,
                () -> repository.insert(new ShortLink(null, secondCode, sessionId, null, 0)));
        assertFalse(failure instanceof ShortLinkCodeConflictException);

        assertNotNull(repository.findByCode(firstCode));
        assertNull(repository.findByCode(secondCode));
    }

    @Test
    void nonexistentSessionIsRejectedByForeignKey() {
        Long missingSessionId = jdbc.queryForObject("""
                SELECT COALESCE(MAX(id), 0) + 1
                FROM study_sessions
                """, Map.of(), Long.class);
        String code = newCode();

        assertThrows(DataIntegrityViolationException.class,
                () -> repository.insert(new ShortLink(null, code, missingSessionId, null, 0)));

        assertNull(repository.findByCode(code));
    }

    private Long createMaterial() {
        Long presenterId = insertAndReturnId("""
                INSERT INTO members
                    (login_id, password_hash, display_name, role, created_at)
                VALUES
                    (:loginId, 'unused-test-hash', '테스트 발표자',
                     'PRESENTER', UTC_TIMESTAMP(3))
                """, new MapSqlParameterSource("loginId", "presenter-" + UUID.randomUUID()));

        jdbc.update("""
                INSERT INTO session_presenters (session_id, member_id, created_at)
                VALUES (:sessionId, :memberId, UTC_TIMESTAMP(3))
                """, new MapSqlParameterSource()
                .addValue("sessionId", sessionId)
                .addValue("memberId", presenterId));

        return insertAndReturnId("""
                INSERT INTO materials
                    (session_id, author_id, title, original_url, created_at, updated_at)
                VALUES
                    (:sessionId, :authorId, '테스트 발표자료',
                     'https://example.com/slides', UTC_TIMESTAMP(3), UTC_TIMESTAMP(3))
                """, new MapSqlParameterSource()
                .addValue("sessionId", sessionId)
                .addValue("authorId", presenterId));
    }

    private Long insertAndReturnId(String sql, MapSqlParameterSource parameters) {
        var keyHolder = new GeneratedKeyHolder();
        jdbc.update(sql, parameters, keyHolder, new String[]{"id"});

        Number key = keyHolder.getKey();
        assertNotNull(key);
        return key.longValue();
    }

    private String newCode() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
