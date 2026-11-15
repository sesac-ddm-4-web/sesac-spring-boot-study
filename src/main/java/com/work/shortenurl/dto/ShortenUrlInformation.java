package com.work.shortenurl.dto;

import com.work.shortenurl.domain.ShortenUrl;

import java.time.LocalDateTime;

public record ShortenUrlInformation (
        String shortenUrlKey,
        String shortenUrl,
        String originalUrl,
        String channel,
        long redirectCount,
        LocalDateTime createdAt,
        LocalDateTime lastRedirectedAt
) {
    public static ShortenUrlInformation of(ShortenUrl shortenUrl, String baseUrl) {
        return new ShortenUrlInformation(
                shortenUrl.getShortenUrlKey(),
                baseUrl + "/" + shortenUrl.getShortenUrlKey(),
                shortenUrl.getOriginalUrl(),
                shortenUrl.getChannel(),
                shortenUrl.getRedirectCount(),
                shortenUrl.getCreatedAt(),
                shortenUrl.getLastRedirectedAt()
        );
    }
}
