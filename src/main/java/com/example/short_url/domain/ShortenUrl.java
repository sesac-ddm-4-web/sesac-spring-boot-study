package com.example.short_url.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Random;
import java.time.LocalDateTime;
import java.util.UUID;;

@Entity
@Table(name = "short_links")
public class ShortenUrl {
    @Id
    private String shortenUrlKey;
    private String originalUrl;
    private Long redirectCount;
    private LocalDateTime expire;
    private String managementToken = UUID.randomUUID().toString();

    private static final int UNUSED_EXPIRE_DAYS = 7;
    private LocalDateTime lastAccessedAt = LocalDateTime.now();

    public ShortenUrl() {}

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

    public ShortenUrl(String originalUrl, String shortenUrlKey, Long redirectCount,
                      LocalDateTime expire, String managementToken, LocalDateTime lastAccessedAt) {
        this.originalUrl = originalUrl;
        this.shortenUrlKey = shortenUrlKey;
        this.redirectCount = redirectCount;
        this.expire = expire;
        this.managementToken = managementToken;
        this.lastAccessedAt = lastAccessedAt;
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

    public String getManagementToken() {
        return managementToken;
    }

    public LocalDateTime getExpire() {
        return expire;
    }

    public LocalDateTime getLastAccessedAt() { return lastAccessedAt; }

    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }

    public void setShortenUrlKey(String shortenUrlKey) {
        this.shortenUrlKey = shortenUrlKey;
    }

    public void setRedirectCount(Long redirectCount) {
        this.redirectCount = redirectCount;
    }

    public void setExpire(LocalDateTime expire) {
        this.expire = expire;
    }

    public void setManagementToken(String managementToken) {
        this.managementToken = managementToken;
    }

    public void setLastAccessedAt(LocalDateTime lastAccessedAt) {
        this.lastAccessedAt = lastAccessedAt;
    }

    public boolean isExpired(){
        LocalDateTime now = LocalDateTime.now();
        //LocalDateTime deadline = lastAccessedAt.plusSeconds(UNUSED_EXPIRE_DAYS);
        LocalDateTime deadline = lastAccessedAt.plusDays(UNUSED_EXPIRE_DAYS);

        if (expire != null && now.isAfter(expire)) {
            return true;
        }

        return now.isAfter(deadline);
    }

    public boolean TokenMatch(String token){
        return managementToken.equals(token);
    }

    public void expireNow(){
        this.expire = LocalDateTime.now();
    }

    public void access(){
        this.lastAccessedAt = LocalDateTime.now();
    }

    public LocalDateTime getLastAccessTime() {
        return lastAccessedAt;
    }
}
