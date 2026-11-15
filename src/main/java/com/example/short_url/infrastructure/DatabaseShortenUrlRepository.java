package com.example.short_url.infrastructure;

import com.example.short_url.domain.ShortenUrl;
import com.example.short_url.domain.ShortenUrlRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

@Repository
public class DatabaseShortenUrlRepository implements ShortenUrlRepository {
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public DatabaseShortenUrlRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    @Override
    public void saveShortenUrl(ShortenUrl shortenUrl) {
        SqlParameterSource namedParameter = new BeanPropertySqlParameterSource(shortenUrl);

        namedParameterJdbcTemplate.update(
                "INSERT INTO short_links (shorten_url_key, original_url, redirect_count, expire, management_token, last_accessed_at) " +
                        "VALUES (:shortenUrlKey, :originalUrl, :redirectCount, :expire, :managementToken, :lastAccessedAt) ON DUPLICATE KEY " +
                        "UPDATE original_url = :originalUrl, redirect_count = :redirectCount, expire = :expire, management_token = :managementToken, last_accessed_at = :lastAccessedAt",
                namedParameter
        );
    }

    @Override
    public ShortenUrl findShortenUrlByShortenUrlKey(String shortenUrlKey){
        SqlParameterSource namedParameter = new MapSqlParameterSource("shortenUrlKey", shortenUrlKey);

        try {
            return namedParameterJdbcTemplate.queryForObject(
                    "SELECT shorten_url_key, original_url, redirect_count, expire, management_token, last_accessed_at " +
                            "FROM short_links WHERE shorten_url_key=:shortenUrlKey",
                    namedParameter,
                    new BeanPropertyRowMapper<>(ShortenUrl.class)
            );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

}
