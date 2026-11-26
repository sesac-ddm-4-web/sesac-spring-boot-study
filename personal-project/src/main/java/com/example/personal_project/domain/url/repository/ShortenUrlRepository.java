package com.example.personal_project.domain.url.repository;

import com.example.personal_project.domain.url.model.ShortenUrl;

public interface ShortenUrlRepository {
    boolean saveShortenUrlIfAbsent(ShortenUrl shortenUrl);
    ShortenUrl findShortenUrlByShortenUrlKey(String shortenUrlKey);
    ShortenUrl increaseRedirectCount(String shortenUrlKey);
}
