package com.example.short_url.presentation;

import com.example.short_url.domain.ShortenUrl;

public class ShortenUrlInformationDto {
    private String originalUrl;
    private String shortenUrlKey;
    private Long reidrctcount;

    public ShortenUrlInformationDto(ShortenUrl shortenUrl){
        this.originalUrl = shortenUrl.getOriginalUrl();
        this.shortenUrlKey = shortenUrl.getShortenUrlKey();
        this.reidrctcount = shortenUrl.getRedirectCount();
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public String getShortenUrlKey() {
        return shortenUrlKey;
    }

    public Long getRedirectcount() {
        return reidrctcount;
    }
}
