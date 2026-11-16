package com.example.personal_project.study;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import java.util.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Repository
public class StudyStore {
    private final NamedParameterJdbcTemplate jdbc;
    public StudyStore(NamedParameterJdbcTemplate jdbc) { this.jdbc = jdbc; }

    public record Member(Long id, String loginId, String passwordHash, String displayName, String role) {}
    public record Material(Long id, Long sessionId, Long authorId, String title, String originalUrl) {}
    public Member member(long id) { return memberQuery("id = :value", id); }
    public Member memberByLogin(String login) { return memberQuery("login_id = :value", login); }
    private Member memberQuery(String condition, Object value) {
        var rows = jdbc.query("SELECT id,login_id,password_hash,display_name,role FROM members WHERE " + condition,
                Map.of("value", value), (r,n) -> new Member(r.getLong(1),r.getString(2),r.getString(3),r.getString(4),r.getString(5)));
        return rows.isEmpty() ? null : rows.getFirst();
    }
    public Material material(long id) {
        var rows = jdbc.query("SELECT id,session_id,author_id,title,original_url FROM materials WHERE id=:id", Map.of("id",id),
                (r,n) -> new Material(r.getLong(1),r.getLong(2),r.getLong(3),r.getString(4),r.getString(5)));
        return rows.isEmpty() ? null : rows.getFirst();
    }
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
    public long insertMaterial(long sessionId, long authorId, String title, String url) {
        var key = new GeneratedKeyHolder();
        jdbc.update("""
                INSERT INTO materials(session_id,author_id,title,original_url,created_at,updated_at)
                VALUES(:sid,:uid,:title,:url,UTC_TIMESTAMP(3),UTC_TIMESTAMP(3))
                """, new MapSqlParameterSource().addValue("sid",sessionId).addValue("uid",authorId)
                .addValue("title",title).addValue("url",url), key, new String[]{"id"});
        if (key.getKey() == null) throw new IllegalStateException("자료 ID를 가져오지 못했습니다.");
        return key.getKey().longValue();
    }
    public void updateMaterial(long id, String title, String url) {
        jdbc.update("UPDATE materials SET title=:title,original_url=:url,updated_at=UTC_TIMESTAMP(3) WHERE id=:id",
                Map.of("id",id,"title",title,"url",url));
    }
    public void updateSession(com.example.personal_project.domain.StudySession s) {
        jdbc.update("""
                UPDATE study_sessions SET session_no=:number,title=:title,starts_at=:startsAt,notice=:notice,
                location=:location,meeting_url=:meetingUrl,preparation=:preparation,updated_at=UTC_TIMESTAMP(3)
                WHERE id=:id
                """,new MapSqlParameterSource().addValue("number",s.sessionNo()).addValue("title",s.title())
                .addValue("startsAt",s.startsAt()).addValue("notice",s.notice()).addValue("location",s.location())
                .addValue("meetingUrl",s.meetingUrl()).addValue("preparation",s.preparation()).addValue("id",s.id()));
    }
    public List<Map<String,Object>> presenters() {
        return jdbc.query("SELECT id,display_name FROM members WHERE role='PRESENTER' ORDER BY id", Map.of(),
                (r,n) -> Map.<String,Object>of("id",r.getLong(1),"displayName",r.getString(2)));
    }
    public List<Map<String,Object>> sessionPresenters(long id) {
        return jdbc.query("""
                SELECT m.id,m.display_name FROM members m JOIN session_presenters p ON m.id=p.member_id
                WHERE p.session_id=:id ORDER BY m.id
                """,Map.of("id",id),(r,n) -> Map.<String,Object>of("id",r.getLong(1),"displayName",r.getString(2)));
    }
    private static String like(String query) {
        return "%" + query.replace("!","!!").replace("%","!%").replace("_","!_") + "%";
    }
    public List<Map<String,Object>> sessions(String query, Long memberId, String base) {
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
        return jdbc.query(sql + " ORDER BY s.session_no DESC",args,(r,n) -> Map.<String,Object>of(
                "id",r.getLong("id"),"sessionNo",r.getInt("session_no"),"title",r.getString("title"),
                "startsAt",r.getObject("starts_at",LocalDateTime.class).atOffset(ZoneOffset.UTC),
                "shortUrl",base+"/study/"+r.getString("code"),"materialCount",r.getLong("material_count")));
    }
    public List<Map<String,Object>> materials(String query, Long sessionId, Long authorId, Long materialId, String base) {
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
            var row = new LinkedHashMap<String,Object>();
            row.put("id",r.getLong("id")); row.put("sessionId",r.getLong("session_id"));
            row.put("sessionTitle",r.getString("session_title"));
            row.put("author",Map.of("id",r.getLong("author_id"),"displayName",r.getString("display_name")));
            row.put("title",r.getString("title")); row.put("originalUrl",r.getString("original_url"));
            row.put("code",r.getString("code")); row.put("shortUrl",base+"/study/"+r.getString("code"));
            row.put("redirectCount",r.getLong("redirect_count")); return row;
        });
    }
    public Map<String,Object> sessionLink(long id) {
        var rows = jdbc.query("SELECT code,redirect_count FROM short_links WHERE session_id=:id",Map.of("id",id),
                (r,n) -> Map.<String,Object>of("code",r.getString(1),"redirectCount",r.getLong(2)));
        if (rows.isEmpty()) throw StudyException.missing();
        return rows.getFirst();
    }
}
