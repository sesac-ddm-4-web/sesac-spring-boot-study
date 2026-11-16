package com.example.personal_project.infrastructure.repository;

import com.example.personal_project.domain.ShortLink;
import com.example.personal_project.domain.ShortLinkRepository;
import com.example.personal_project.global.exception.ShortLinkCodeConflictException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.sql.SQLException;

@Repository
public class JdbcShortLinkRepository implements ShortLinkRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public JdbcShortLinkRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    @Override
    public void insert(ShortLink shortLink) {
        String sql =
                """
                INSERT INTO short_links
                    (code, session_id, material_id, redirect_count, created_at)
                VALUES
                    (:code, :sessionId, :materialId, 0, UTC_TIMESTAMP(3))
                """;

        var parameters = new MapSqlParameterSource()
                .addValue("code", shortLink.code())
                .addValue("sessionId", shortLink.sessionId())
                .addValue("materialId", shortLink.materialId());

        try {
            namedParameterJdbcTemplate.update(sql, parameters);
        } catch (DuplicateKeyException exception) {
            // MySQL 1062의 인덱스 이름으로 코드 충돌과 대상 중복을 구분한다.
            if (exception.getMostSpecificCause() instanceof SQLException sqlException
                    && sqlException.getErrorCode() == 1062
                    && sqlException.getMessage() != null
                    && sqlException.getMessage().contains("uk_short_links_code")) {
                throw new ShortLinkCodeConflictException(exception);
            }
            throw exception;
        }
    }

    @Override
    public ShortLink findByCode(String code) {
        String sql =
                """
                SELECT id, code, session_id, material_id, redirect_count
                FROM short_links
                WHERE code = :code
                """;

        List<ShortLink> results = namedParameterJdbcTemplate.query(
                sql,
                Map.of("code", code),
                (rs, rowNum) -> new ShortLink(
                        rs.getLong("id"),
                        rs.getString("code"),
                        rs.getObject("session_id", Long.class),
                        rs.getObject("material_id", Long.class),
                        rs.getLong("redirect_count")
                )
        );

        return results.isEmpty() ? null : results.getFirst();
    }

    @Override
    public int incrementRedirectCount(String code) {
        return namedParameterJdbcTemplate.update(
                """
                UPDATE short_links
                SET redirect_count = redirect_count + 1
                WHERE code = :code
                """,
                Map.of("code", code)
        );
    }
}
