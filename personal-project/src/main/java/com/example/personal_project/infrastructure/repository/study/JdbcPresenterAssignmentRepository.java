package com.example.personal_project.infrastructure.repository.study;

import com.example.personal_project.domain.study.repository.PresenterAssignmentRepository;
import java.util.*;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcPresenterAssignmentRepository implements PresenterAssignmentRepository {
    private final NamedParameterJdbcTemplate jdbc;
    public JdbcPresenterAssignmentRepository(NamedParameterJdbcTemplate jdbc) { this.jdbc = jdbc; }
    public boolean assigned(long sessionId, long memberId) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM session_presenters WHERE session_id=:sid AND member_id=:uid",
                Map.of("sid",sessionId,"uid",memberId), Long.class) > 0;
    }
    public void assign(long sessionId, long memberId) {
        jdbc.update("""
                INSERT INTO session_presenters(session_id,member_id,created_at)
                VALUES(:sid,:uid,UTC_TIMESTAMP(3))
                ON DUPLICATE KEY UPDATE member_id=VALUES(member_id)
                """, Map.of("sid",sessionId,"uid",memberId));
    }

}
