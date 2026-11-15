package com.work.shortenurl.repository;


import com.work.shortenurl.domain.ShortenUrl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ShortenUrlJpaRepository extends JpaRepository<ShortenUrl, Long> {

    Optional<ShortenUrl> findByShortenUrlKey(String shortenUrlKey);

    boolean existsByShortenUrlKey(String shortenUrlKey);

    List<ShortenUrl> findAllByOrderByCreatedAtDesc();

    List<ShortenUrl> findAllByOriginalUrlOrderByRedirectCountDesc(String originalUrl);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE ShortenUrl s " +
            "SET s.redirectCount = s.redirectCount + 1, s.lastRedirectedAt = :now " +
            "WHERE s.shortenUrlKey = :key")
    int increaseRedirectCount(@Param("key") String shortenUrlKey, @Param("now") LocalDateTime now);

}
