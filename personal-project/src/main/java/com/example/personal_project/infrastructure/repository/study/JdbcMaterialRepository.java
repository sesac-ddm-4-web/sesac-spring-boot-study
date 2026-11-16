package com.example.personal_project.infrastructure.repository.study;

import com.example.personal_project.domain.study.model.Material;
import com.example.personal_project.domain.study.repository.MaterialRepository;
import java.util.*;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcMaterialRepository implements MaterialRepository {
    private final NamedParameterJdbcTemplate jdbc;
    public JdbcMaterialRepository(NamedParameterJdbcTemplate jdbc) { this.jdbc = jdbc; }
    public Material material(long id) {
        var rows = jdbc.query("SELECT id,session_id,author_id,title,original_url FROM materials WHERE id=:id", Map.of("id",id),
                (r,n) -> new Material(r.getLong(1),r.getLong(2),r.getLong(3),r.getString(4),r.getString(5)));
        return rows.isEmpty() ? null : rows.getFirst();
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

}
