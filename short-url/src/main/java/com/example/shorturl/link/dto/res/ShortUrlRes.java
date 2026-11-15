package com.example.shorturl.link.dto.res;

import com.example.shorturl.link.domain.LinkStatus;
import com.example.shorturl.link.domain.ShortUrl;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ShortUrlRes(
        String key,
        String shortPath,
        String originalUrl,
        String title,
        boolean custom,
        LocalDateTime createdAt,
        LocalDate expiresOn,
        LinkStatus status,
        long visitCount,
        long botVisitCount,
        LocalDateTime firstVisitedAt,
        LocalDateTime lastVisitedAt
) {
    public static ShortUrlRes of(ShortUrl shortUrl, LocalDate today) {
        return new ShortUrlRes(
                shortUrl.getKey(),
                "/" + shortUrl.getKey(),
                shortUrl.getOriginalUrl(),
                shortUrl.getTitle(),
                shortUrl.isCustom(),
                shortUrl.getCreatedAt(),
                shortUrl.getExpiresOn(),
                shortUrl.status(today),
                shortUrl.getVisitCount(),
                shortUrl.getBotVisitCount(),
                shortUrl.getFirstVisitedAt(),
                shortUrl.getLastVisitedAt()
        );
    }
}
