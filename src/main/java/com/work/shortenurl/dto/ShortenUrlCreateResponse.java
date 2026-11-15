package com.work.shortenurl.dto;

import com.work.shortenurl.domain.ShortenUrl;

import java.time.LocalDateTime;

public record ShortenUrlCreateResponse(
        String shortenUrlKey,
        String shortenUrl,
        String originalUrl,
        String channel,
        LocalDateTime createdAt
) {
    public static ShortenUrlCreateResponse of(ShortenUrl shortenUrl, String baseUrl) {
        return new ShortenUrlCreateResponse(
                shortenUrl.getShortenUrlKey(),
                baseUrl + "/" + shortenUrl.getShortenUrlKey(),
                shortenUrl.getOriginalUrl(),
                shortenUrl.getChannel(),
                shortenUrl.getCreatedAt()
        );
    }
}
