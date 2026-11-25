package com.example.shortlab.infrastructure;

import com.example.shortlab.domain.ShortenUrl;
import com.example.shortlab.domain.ShortenUrlRepository;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MapShortenUrlRepository implements ShortenUrlRepository {

    private Map<String, ShortenUrl> shortenUrls = new ConcurrentHashMap<>();

    @Override
    public void saveShortenUrl(ShortenUrl shortenUrl) {
        shortenUrls.put(shortenUrl.getShortenUrlKey(), shortenUrl);
    }

    @Override
    public ShortenUrl findShortenUrlByShortenUrlKey(String shortenUrlKey) {
        return shortenUrls.get(shortenUrlKey);
    }

    @Override
    public void incrementRedirectCount(String shortenUrlKey) {
        ShortenUrl shortenUrl = shortenUrls.get(shortenUrlKey);
        if (shortenUrl != null) shortenUrl.increaseRedirectCount();
    }

    @Override
    public boolean existsByShortenUrlKey(String shortenUrlKey) {
        return shortenUrls.containsKey(shortenUrlKey);
    }
}
