package com.example.short_url.presentation;

import com.example.short_url.domain.ShortenUrl;

public class ShortenUrlCreateResponseDto {
    private String originalUrl;
    private String shortenUrlKey;
    private String managementToken;

    public ShortenUrlCreateResponseDto(ShortenUrl shortenUrl){
        this.originalUrl = shortenUrl.getOriginalUrl();
        this.shortenUrlKey = shortenUrl.getShortenUrlKey();
        this.managementToken = shortenUrl.getManagementToken();
    }

    public String getOriginalUrl(){
        return originalUrl;
    }

    public String getShortenUrlKey(){
        return shortenUrlKey;
    }

    public String getManagementToken() { return managementToken; }
}
