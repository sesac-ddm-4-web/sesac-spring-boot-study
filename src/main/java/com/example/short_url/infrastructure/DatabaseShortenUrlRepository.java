package com.example.short_url.infrastructure;

import com.example.short_url.domain.JpaShortenUrlRepository;
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
    //private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final JpaShortenUrlRepository jpaRepository;

    //public DatabaseShortenUrlRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        //this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    //}
    public DatabaseShortenUrlRepository(JpaShortenUrlRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }



    @Override
    public void saveShortenUrl(ShortenUrl shortenUrl) {
        jpaRepository.save(shortenUrl);
    }

    @Override
    public ShortenUrl findShortenUrlByShortenUrlKey(String shortenUrlKey){
        return jpaRepository.findById(shortenUrlKey).orElse(null);
    }

}
