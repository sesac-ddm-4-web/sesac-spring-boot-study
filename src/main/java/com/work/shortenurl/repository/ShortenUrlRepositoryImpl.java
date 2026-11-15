package com.work.shortenurl.repository;

import com.work.shortenurl.domain.ShortenUrl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ShortenUrlRepositoryImpl implements ShortenUrlRepository {

    private final ShortenUrlJpaRepository jpaRepository;

    @Override
    public ShortenUrl save(ShortenUrl shortenUrl) {
        return jpaRepository.save(shortenUrl);
    }

    @Override
    public Optional<ShortenUrl> findByShortenUrlKey(String shortenUrlKey) {
        return jpaRepository.findByShortenUrlKey(shortenUrlKey);
    }

    @Override
    public boolean existsByShortenUrlKey(String shortenUrlKey) {
        return jpaRepository.existsByShortenUrlKey(shortenUrlKey);
    }

    @Override
    public List<ShortenUrl> findAllOrderByCreatedAtDesc() {
        return jpaRepository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    public List<ShortenUrl> findAllByOriginalUrlOrderByRedirectCountDesc(String originalUrl) {
        return jpaRepository.findAllByOriginalUrlOrderByRedirectCountDesc(originalUrl);
    }

    @Override
    public int increaseRedirectCount(String shortenUrlKey, LocalDateTime now) {
        return jpaRepository.increaseRedirectCount(shortenUrlKey, now);
    }

}