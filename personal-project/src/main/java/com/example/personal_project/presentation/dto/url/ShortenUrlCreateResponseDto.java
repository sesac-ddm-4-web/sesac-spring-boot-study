package com.example.personal_project.presentation.dto.url;

import com.example.personal_project.application.url.result.ShortenUrlResult;

public class ShortenUrlCreateResponseDto {
    private String originalUrl;
    private String shortenUrlKey;

    public ShortenUrlCreateResponseDto(ShortenUrlResult shortenUrl) {
        this.originalUrl = shortenUrl.getOriginalUrl();
        this.shortenUrlKey = shortenUrl.getShortenUrlKey();
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public String getShortenUrlKey() {
        return shortenUrlKey;
    }
}
