package com.example.personal_project.infrastructure.repository;

import com.example.personal_project.domain.ShortenUrl;
import com.example.personal_project.domain.ShortenUrlRepository;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class MapShortenUrlRepository implements ShortenUrlRepository {

    private Map<String, ShortenUrl> shortenUrls = new ConcurrentHashMap<>();

    @Override
    public boolean saveShortenUrlIfAbsent(ShortenUrl shortenUrl) {
        ShortenUrl existing = shortenUrls.putIfAbsent(
                shortenUrl.getShortenUrlKey(),
                shortenUrl
        );

        return existing == null;
    }

    @Override
    public ShortenUrl findShortenUrlByShortenUrlKey(String shortenUrlKey) {
        ShortenUrl shortenUrl = shortenUrls.get(shortenUrlKey);
        return shortenUrl;
    }

    @Override
    public ShortenUrl increaseRedirectCount(String shortenUrlKey) {
        return shortenUrls.computeIfPresent(
                shortenUrlKey,
                (key, existing) -> {
                    existing.increaseRedirectCount();
                    return existing;
                }
        );
    }
}
