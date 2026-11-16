package com.example.personal_project.infrastructure.repository;

import com.example.personal_project.domain.StudySession;
import com.example.personal_project.domain.StudySessionRepository;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Map;

@Repository
public class JdbcStudySessionRepository implements StudySessionRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public JdbcStudySessionRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Long insert(StudySession session) {
        String sql = """
                INSERT INTO study_sessions
                    (session_no, title, starts_at, notice, location, meeting_url, preparation, created_by, created_at, updated_at)
                VALUES
                    (:sessionNo, :title, :startsAt, :notice, :location, :meetingUrl, :preparation, :createdBy, UTC_TIMESTAMP(3), UTC_TIMESTAMP(3))
                """;

        var parameters = new MapSqlParameterSource()
                .addValue("sessionNo", session.sessionNo())
                .addValue("title", session.title())
                .addValue("startsAt", session.startsAt())
                .addValue("notice", session.notice())
                .addValue("location", session.location())
                .addValue("meetingUrl", session.meetingUrl())
                .addValue("preparation", session.preparation())
                .addValue("createdBy", session.createdBy());

        var keyHolder = new GeneratedKeyHolder();
        jdbc.update(sql, parameters, keyHolder, new String[]{"id"});
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("저장된 회차의 ID를 가져오지 못했습니다.");
        }
        return key.longValue();
    }

    @Override
    public StudySession findById(Long id) {
        String sql = """
                SELECT id, session_no, title, starts_at, notice, location, meeting_url, preparation, created_by, created_at, updated_at
                FROM study_sessions
                WHERE id = :id
                """;

        var results = jdbc.query(sql, Map.of("id", id), (rs, rowNum) -> new StudySession(
                rs.getLong("id"),
                rs.getInt("session_no"),
                rs.getString("title"),
                rs.getObject("starts_at", LocalDateTime.class),
                rs.getString("notice"),
                rs.getString("location"),
                rs.getString("meeting_url"),
                rs.getString("preparation"),
                rs.getLong("created_by"),
                rs.getObject("created_at", LocalDateTime.class),
                rs.getObject("updated_at", LocalDateTime.class)
        ));
        return results.isEmpty() ? null : results.getFirst();
    }
}
