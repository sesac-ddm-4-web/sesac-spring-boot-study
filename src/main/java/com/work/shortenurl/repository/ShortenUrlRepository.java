package com.work.shortenurl.repository;

import com.work.shortenurl.domain.ShortenUrl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ShortenUrlRepository {

    ShortenUrl save(ShortenUrl shortenUrl);

    Optional<ShortenUrl> findByShortenUrlKey(String shortenUrlKey);

    boolean existsByShortenUrlKey(String shortenUrlKey);

    List<ShortenUrl> findAllOrderByCreatedAtDesc();

    List<ShortenUrl> findAllByOriginalUrlOrderByRedirectCountDesc(String originalUrl);

    int increaseRedirectCount(String shortenUrlKey, LocalDateTime now);
}
