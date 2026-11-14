package com.example.short_url.domain;

import java.util.List;
import java.util.Random;
import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

public class ShortenUrl {
    private String originalUrl;
    private String shortenUrlKey;
    private Long redirectCount;
    private LocalDateTime expire;

    public static String generateShortenUrlKey(){
        String base56Characters="23456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz";
        Random random = new Random();
        StringBuilder shortenUrlKey = new StringBuilder();

        for (int count=0; count<8; count++){
            int base56CharactersIndex = random.nextInt(0,base56Characters.length());
            char base56Character = base56Characters.charAt(base56CharactersIndex);
            shortenUrlKey.append(base56Character);
        }
        return shortenUrlKey.toString();
    }

    public ShortenUrl(String originalUrl, String shortenUrlKey, LocalDateTime expire){
        this.originalUrl = originalUrl;
        this.shortenUrlKey = shortenUrlKey;
        this.redirectCount = 0L;
        this.expire = expire;
    }

    public ShortenUrl(String originalUrl, String shortenUrlKey){
        this(originalUrl, shortenUrlKey, null);
    }

    public void increaseRedirectCount() {
        this.redirectCount = this.redirectCount+1;
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

    public LocalDateTime getExpire() {
        return expire;
    }

    public boolean isExpired(){
        if (expire == null) {
            return false;                       // 만료 시각이 없으면 영구 링크 → 만료 아님
        }

        LocalDateTime now = LocalDateTime.now(); // 지금 시각
        return now.isAfter(expire);
    }
}
