package com.example.shorten_url_service.presentation;

import com.example.shorten_url_service.domain.ShortenUrl;

// ShortenURl -> 생성 응답 DTO
// 생성 결과로 원본주소와 단축키 보내기
public class ShortenUrlCreateResponseDto {
    private String originalUrl;
    private String shortenUrlKey;

    public ShortenUrlCreateResponseDto(ShortenUrl shortenUrl) {
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
