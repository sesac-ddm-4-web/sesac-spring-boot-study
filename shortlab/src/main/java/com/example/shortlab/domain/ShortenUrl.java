package com.example.shortlab.domain;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public class ShortenUrl {
    private String originalUrl;
    private String shortenUrlKey;
    private final AtomicLong redirectCount = new AtomicLong(0);

    public ShortenUrl(String originalUrl, String shortenUrlKey) {
        this.originalUrl = originalUrl;
        this.shortenUrlKey = shortenUrlKey;
    }

    public ShortenUrl(String originalUrl, String shortenUrlKey, long redirectCount) {
        this.originalUrl = originalUrl;
        this.shortenUrlKey = shortenUrlKey;
        this.redirectCount.set(redirectCount);
    }

    // 리다이렉트 시 카운트 증가 (스레드 안전)
    public void increaseRedirectCount() {
        redirectCount.incrementAndGet();
    }


    // 단축 url 키 생성 알고리즘
    public static String generateShortenUrlKey(){
        String base56Characters = "23456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz";

        Random random = new Random();
        StringBuilder shortenUrlKey = new StringBuilder();

        for(int count = 0; count < 8; count++){
            int base56CharactersIndex = random.nextInt(0, base56Characters.length());
            char base56Character = base56Characters.charAt(base56CharactersIndex);
            shortenUrlKey.append(base56Character);
        }

        return shortenUrlKey.toString();
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public String getShortenUrlKey() {
        return shortenUrlKey;
    }

    public Long getRedirectCount() {
        return redirectCount.get();
    }

}
