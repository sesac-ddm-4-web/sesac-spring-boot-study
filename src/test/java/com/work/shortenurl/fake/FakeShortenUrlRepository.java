package com.work.shortenurl.fake;

import com.work.shortenurl.domain.ShortenUrl;
import com.work.shortenurl.repository.ShortenUrlRepository;

import java.time.LocalDateTime;
import java.util.*;

public class FakeShortenUrlRepository implements ShortenUrlRepository {

    private final Map<String, ShortenUrl> store = new HashMap<>();

    @Override
    public ShortenUrl save(ShortenUrl shortenUrl) {
        store.put(shortenUrl.getShortenUrlKey(), shortenUrl);
        return shortenUrl;
    }

    @Override
    public Optional<ShortenUrl> findByShortenUrlKey(String shortenUrlKey) {
        return Optional.ofNullable(store.get(shortenUrlKey));
    }

    @Override
    public boolean existsByShortenUrlKey(String shortenUrlKey) {
        return store.containsKey(shortenUrlKey);
    }

    @Override
    public List<ShortenUrl> findAllOrderByCreatedAtDesc() {
        return store.values().stream()
                .sorted(Comparator.comparing(ShortenUrl::getCreatedAt).reversed())
                .toList();
    }

    @Override
    public List<ShortenUrl> findAllByOriginalUrlOrderByRedirectCountDesc(String originalUrl) {
        return store.values().stream()
                .filter(s -> s.getOriginalUrl().equals(originalUrl))
                .sorted(Comparator.comparingLong(ShortenUrl::getRedirectCount).reversed())
                .toList();
    }

    @Override
    public int increaseRedirectCount(String shortenUrlKey, LocalDateTime now) {
        throw new UnsupportedOperationException("리다이렉트 카운트는 JPA 테스트에서 검증");
    }
}