package com.example.shorten_url_service.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ShortenUrlJpaRepository extends JpaRepository<ShortenUrlEntity, Long> {
    boolean existsByOriginalUrl(String originalUrl);

    Optional<ShortenUrlEntity> findByShortenUrlKey(String shortenUrlKey);

    @Modifying
    @Query("""
        UPDATE ShortenUrlEntity url
        SET url.redirectCount = url.redirectCount + 1
        WHERE url.shortenUrlKey = :shortenUrlKey
        """)
    void increaseRedirectCount(@Param("shortenUrlKey") String shortenUrlKey);
}