package io.github.tkdgh816.brevlet.domain.url;

import java.time.Instant;

public class ShortenUrl {
    private final String originalUrl;
    private final String shortenUrlKey;
    private Long redirectCount;
    private final Instant expiresAt;

    public ShortenUrl(String originalUrl, String shortenUrlKey, Instant expiresAt) {
        this.originalUrl = originalUrl;
        this.shortenUrlKey = shortenUrlKey;
        this.redirectCount = 0L;
        this.expiresAt = expiresAt;
    }

    private ShortenUrl(String originalUrl, String shortenUrlKey, Long redirectCount, Instant expiresAt) {
        this.originalUrl = originalUrl;
        this.shortenUrlKey = shortenUrlKey;
        this.redirectCount = redirectCount;
        this.expiresAt = expiresAt;
    }

    public static ShortenUrl restore(String originalUrl, String shortenUrlKey, Long redirectCount, Instant expiresAt) {
        return new ShortenUrl(originalUrl, shortenUrlKey, redirectCount, expiresAt);
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public String getShortenUrlKey() {
        return shortenUrlKey;
    }

    public Long getRedirectCount() {
        return redirectCount;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void increaseRedirectCount() {
        redirectCount++;
    }

    public boolean isAvailable(Instant now) {
        return expiresAt == null || expiresAt.isAfter(now);
    }
}
