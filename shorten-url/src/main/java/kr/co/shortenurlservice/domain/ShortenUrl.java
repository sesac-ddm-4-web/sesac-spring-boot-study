package kr.co.shortenurlservice.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Random;

@Entity
@Table(name = "shorten_urls")
public class ShortenUrl {

    @Column(name = "original_url", nullable = false, length = 2048)
    private String originalUrl;

    @Id
    @Column(name = "shorten_url_key", length = 8)
    private String shortenUrlKey;

    @Column(name = "redirect_count", nullable = false)
    private Long redirectCount;

    @Column(name = "active", nullable = false)
    private boolean active;

    protected ShortenUrl() {
    }

    public ShortenUrl(String originalUrl, String shortenUrlKey) {
        this(originalUrl, shortenUrlKey, 0L, true);
    }

    public ShortenUrl(
            String originalUrl,
            String shortenUrlKey,
            Long redirectCount,
            boolean active
    ) {
        this.originalUrl = originalUrl;
        this.shortenUrlKey = shortenUrlKey;
        this.redirectCount = redirectCount;
        this.active = active;
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

    public boolean isActive() {
        return active;
    }

    public static String generateShortenUrlKey() {

        String characters =
                "23456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz";

        Random random = new Random();
        StringBuilder key = new StringBuilder();

        for (int i = 0; i < 8; i++) {
            int index = random.nextInt(characters.length());
            key.append(characters.charAt(index));
        }

        return key.toString();
    }
}
