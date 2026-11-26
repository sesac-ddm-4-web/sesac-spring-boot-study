package com.example.personal_project.application.url.result;

import com.example.personal_project.domain.url.model.ShortenUrl;

public record ShortenUrlResult(String originalUrl, String shortenUrlKey, Long redirectCount) {
    public static ShortenUrlResult from(ShortenUrl url) { return new ShortenUrlResult(url.getOriginalUrl(), url.getShortenUrlKey(), url.getRedirectCount()); }
    public String getOriginalUrl() { return originalUrl; }
    public String getShortenUrlKey() { return shortenUrlKey; }
    public Long getRedirectCount() { return redirectCount; }
}
