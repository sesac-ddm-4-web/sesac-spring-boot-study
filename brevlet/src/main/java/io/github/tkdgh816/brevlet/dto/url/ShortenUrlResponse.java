package io.github.tkdgh816.brevlet.dto.url;

import io.github.tkdgh816.brevlet.domain.url.ShortenUrl;

import java.time.Instant;

public record ShortenUrlResponse(
        String originalUrl,
        String shortenUrlKey,
        Long redirectCount,
        Instant expiresAt
) {

    public static ShortenUrlResponse from(ShortenUrl shortenUrl) {
        return new ShortenUrlResponse(shortenUrl.getOriginalUrl(), shortenUrl.getShortenUrlKey(), shortenUrl.getRedirectCount(), shortenUrl.getExpiresAt());
    }
}
