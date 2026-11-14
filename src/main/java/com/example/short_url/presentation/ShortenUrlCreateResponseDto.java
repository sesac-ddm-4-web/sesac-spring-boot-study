package com.example.short_url.presentation;

import com.example.short_url.domain.ShortenUrl;

public class ShortenUrlCreateResponseDto {
    private String originalUrl;
    private String shortenUrlKey;

    public ShortenUrlCreateResponseDto(ShortenUrl shortenUrl){
        this.originalUrl = shortenUrl.getOriginalUrl();
        this.shortenUrlKey = shortenUrl.getShortenUrlKey();
    }

    public String getOriginalUrl(){
        return originalUrl;
    }

    public String getShortenUrlKey(){
        return shortenUrlKey;
    }
}
