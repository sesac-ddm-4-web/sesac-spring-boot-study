package com.example.personal_project.presentation.dto.url;

import com.example.personal_project.application.url.result.ShortenUrlResult;

public class ShortenUrlInformationDto {
    private String originalUrl;
    private String shortenUrlKey;
    private Long redirectCount;

    public ShortenUrlInformationDto(ShortenUrlResult shortenUrl) {
        this.originalUrl = shortenUrl.getOriginalUrl();
        this.shortenUrlKey = shortenUrl.getShortenUrlKey();
        this.redirectCount = shortenUrl.getRedirectCount();
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
}
