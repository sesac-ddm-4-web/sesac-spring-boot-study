package com.example.shortlab.domain;

public interface ShortenUrlRepository {
    void saveShortenUrl(ShortenUrl shortenUrl);
    ShortenUrl findShortenUrlByShortenUrlKey(String shortenUrlKey);
    void incrementRedirectCount(String shortenUrlKey);
    boolean existsByShortenUrlKey(String key);
}
