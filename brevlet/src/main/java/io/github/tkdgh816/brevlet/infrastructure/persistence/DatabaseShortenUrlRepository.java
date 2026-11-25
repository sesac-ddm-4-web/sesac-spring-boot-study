package io.github.tkdgh816.brevlet.infrastructure.persistence;

import io.github.tkdgh816.brevlet.domain.key.ShortenUrlKeyConflictException;
import io.github.tkdgh816.brevlet.domain.url.ShortenUrl;
import io.github.tkdgh816.brevlet.domain.url.ShortenUrlRepository;
import org.jspecify.annotations.Nullable;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

@Profile("prod")
@Repository
public class DatabaseShortenUrlRepository implements ShortenUrlRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public DatabaseShortenUrlRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(ShortenUrl shortenUrl) {
        try {
            jdbcTemplate.update("""
                            INSERT INTO shorten_urls
                                (shorten_url_key, original_url, redirect_count, expires_at)
                            VALUES
                                (:shortenUrlKey, :originalUrl, :redirectCount, :expiresAt)
                            """,
                    getSqlParameterSource(shortenUrl)
            );
        } catch (DuplicateKeyException e) {
            throw new ShortenUrlKeyConflictException("이미 존재하는 단축 URL 키입니다.");
        }
    }

    @Override
    public Optional<ShortenUrl> findByKey(String shortenUrlKey) {
        try {
            ShortenUrl shortenUrl = jdbcTemplate.queryForObject("""
                            SELECT * FROM shorten_urls
                            WHERE shorten_url_key = :shortenUrlKey
                            """,
                    new MapSqlParameterSource("shortenUrlKey", shortenUrlKey),
                    ROW_MAPPER
            );
            return Optional.of(shortenUrl);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public void increaseRedirectCount(String shortenUrlKey) {
        jdbcTemplate.update("""
                        UPDATE shorten_urls
                        SET redirect_count = redirect_count + 1
                        WHERE shorten_url_key = :shortenUrlKey
                        """,
                new MapSqlParameterSource("shortenUrlKey", shortenUrlKey)
        );
    }

    private static SqlParameterSource getSqlParameterSource(ShortenUrl shortenUrl) {
        return new BeanPropertySqlParameterSource(shortenUrl) {
            @Override
            public @Nullable Object getValue(String paramName) throws IllegalArgumentException {
                Object value = super.getValue(paramName);
                return value instanceof Instant instant ? toLocalDateTime(instant) : value;
            }
        };
    }


    private static final RowMapper<ShortenUrl> ROW_MAPPER = (rs, rowNum) -> {
        String shortenUrlKey = rs.getString("shorten_url_key");
        String originalUrl = rs.getString("original_url");
        Long redirectCount = rs.getLong("redirect_count");
        Instant expiresAt = toInstant(rs.getObject("expires_at", LocalDateTime.class));

        return ShortenUrl.restore(originalUrl, shortenUrlKey, redirectCount, expiresAt);
    };

    private static LocalDateTime toLocalDateTime(Instant instant) {
        return instant == null
                ? null
                : LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
    }

    private static Instant toInstant(LocalDateTime time) {
        return time == null
                ? null
                : time.toInstant(ZoneOffset.UTC);
    }
}
