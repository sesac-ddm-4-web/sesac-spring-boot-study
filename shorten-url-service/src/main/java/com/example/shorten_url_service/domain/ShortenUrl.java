package com.example.shorten_url_service.domain;

import java.security.SecureRandom;

public class ShortenUrl {
    private String originalUrl;
    private String shortenUrlKey;
    private Long redirectCount;
    private static final SecureRandom RANDOM = new SecureRandom();

    public ShortenUrl(String originalUrl, String shortenUrlKey) {
        this(originalUrl, shortenUrlKey, 0L);
    }

    public ShortenUrl(String originalUrl, String shortenUrlKey, Long redirectCount) {
        this.originalUrl = originalUrl;
        this.shortenUrlKey = shortenUrlKey;
        this.redirectCount = redirectCount;
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

    public void increaseRedirectCount() {
        redirectCount++;
    }

    public static String generateShortenUrlKey() {
        StringBuilder shortenUrlKey = new StringBuilder(8);
        for (int count = 0; count < 8; count++) {
            int digit = RANDOM.nextInt(10);
            shortenUrlKey.append(digit);
        }
        return shortenUrlKey.toString();
    }
}
