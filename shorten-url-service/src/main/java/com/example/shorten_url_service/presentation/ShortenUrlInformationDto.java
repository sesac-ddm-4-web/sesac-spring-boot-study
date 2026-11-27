package com.example.shorten_url_service.presentation;

import com.example.shorten_url_service.domain.ShortenUrl;

// 조회결과 + 이동횟수
public class ShortenUrlInformationDto {
    private String originalUrl;
    private String shortenUrl;
    private Long redirectCount;

    public ShortenUrlInformationDto(ShortenUrl shortenUrl) {
        this.originalUrl = shortenUrl.getOriginalUrl();
        this.shortenUrl = shortenUrl.getShortenUrlKey();
        this.redirectCount = shortenUrl.getRedirectCount();
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public String getShortenUrlKey() {
        return shortenUrl;
    }

    public Long getRedirectCount() {
        return redirectCount;
    }
}
