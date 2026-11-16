package com.example.personal_project.domain;

public interface ShortenUrlRepository {
    boolean saveShortenUrlIfAbsent(ShortenUrl shortenUrl);
    ShortenUrl findShortenUrlByShortenUrlKey(String shortenUrlKey);
    ShortenUrl increaseRedirectCount(String shortenUrlKey);
}
