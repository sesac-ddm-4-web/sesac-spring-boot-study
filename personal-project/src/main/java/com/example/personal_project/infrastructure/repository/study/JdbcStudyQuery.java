package com.example.personal_project.infrastructure.repository.study;

import com.example.personal_project.application.study.port.StudyQueryPort;
import com.example.personal_project.application.study.result.StudyViews.*;
import com.example.personal_project.domain.study.exception.StudyException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcStudyQuery implements StudyQueryPort {
    private final NamedParameterJdbcTemplate jdbc;
    public JdbcStudyQuery(NamedParameterJdbcTemplate jdbc) { this.jdbc = jdbc; }
    public List<Presenter> presenters() {
        return jdbc.query("SELECT id,display_name FROM members WHERE role='PRESENTER' ORDER BY id", Map.of(),
                (r,n) -> new Presenter(r.getLong(1), r.getString(2)));
    }
    public List<Presenter> sessionPresenters(long id) {
        return jdbc.query("""
                SELECT m.id,m.display_name FROM members m JOIN session_presenters p ON m.id=p.member_id
                WHERE p.session_id=:id ORDER BY m.id
                """,Map.of("id",id),(r,n) -> new Presenter(r.getLong(1), r.getString(2)));
    }
    private static String like(String query) {
        return "%" + query.replace("!","!!").replace("%","!%").replace("_","!_") + "%";
    }
    public List<SessionSummary> sessions(String query, Long memberId) {
        String sql = """
                SELECT s.id,s.session_no,s.title,s.starts_at,l.code,
                (SELECT COUNT(*) FROM materials m WHERE m.session_id=s.id) AS material_count
                FROM study_sessions s JOIN short_links l ON l.session_id=s.id
                WHERE s.title LIKE :query ESCAPE '!'
                """;
        var args = new MapSqlParameterSource("query", like(query));
        if (memberId != null) {
            sql += " AND EXISTS(SELECT 1 FROM session_presenters p WHERE p.session_id=s.id AND p.member_id=:uid)";
            args.addValue("uid",memberId);
        }
        return jdbc.query(sql + " ORDER BY s.session_no DESC",args,(r,n) -> new SessionSummary(r.getLong("id"),r.getInt("session_no"),r.getString("title"),
                r.getObject("starts_at",LocalDateTime.class).atOffset(ZoneOffset.UTC),r.getString("code"),r.getLong("material_count")));
    }
    public List<MaterialView> materials(String query, Long sessionId, Long authorId, Long materialId) {
        String sql = """
                SELECT m.id,m.session_id,s.title AS session_title,m.author_id,u.display_name,m.title,
                m.original_url,l.code,l.redirect_count
                FROM materials m JOIN study_sessions s ON m.session_id=s.id
                JOIN members u ON m.author_id=u.id JOIN short_links l ON l.material_id=m.id
                WHERE m.title LIKE :query ESCAPE '!'
                """;
        var args = new MapSqlParameterSource("query",like(query));
        if (sessionId != null) { sql += " AND m.session_id=:sid"; args.addValue("sid",sessionId); }
        if (authorId != null) { sql += " AND m.author_id=:uid"; args.addValue("uid",authorId); }
        if (materialId != null) { sql += " AND m.id=:mid"; args.addValue("mid",materialId); }
        return jdbc.query(sql+" ORDER BY m.created_at ASC,m.id ASC",args,(r,n) -> {
            return new MaterialView(r.getLong("id"), r.getLong("session_id"), r.getString("session_title"),
                    new Presenter(r.getLong("author_id"), r.getString("display_name")), r.getString("title"),
                    r.getString("original_url"), r.getString("code"), r.getLong("redirect_count"));
        });
    }
    public Link sessionLink(long id) {
        var rows = jdbc.query("SELECT code,redirect_count FROM short_links WHERE session_id=:id",Map.of("id",id),
                (r,n) -> new Link(r.getString(1), r.getLong(2)));
        if (rows.isEmpty()) throw StudyException.missing();
        return rows.getFirst();
    }

}
