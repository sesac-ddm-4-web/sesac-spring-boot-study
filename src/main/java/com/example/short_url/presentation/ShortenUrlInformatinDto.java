package com.example.short_url.presentation;

import com.example.short_url.domain.ShortenUrl;

public class ShortenUrlInformatinDto {
    private String originalUrl;
    private String shortenUrlKey;
    private Long reidrctcount;

    public ShortenUrlInformatinDto(ShortenUrl shortenUrl){
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

    public Long getReidrctcount() {
        return reidrctcount;
    }
}
