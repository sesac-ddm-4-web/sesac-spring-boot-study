package com.example.shorten_url_service.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "shorten_urls")
public class ShortenUrlEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "original_url", nullable = false, columnDefinition = "LONGTEXT")
    private String originalUrl;

    @Column(name = "shorten_url_key", nullable = false, unique = true, length = 8)
    private String shortenUrlKey;

    @Column(name = "redirect_count", nullable = false)
    private Long redirectCount;

    protected ShortenUrlEntity() {
    }

    public ShortenUrlEntity(String originalUrl, String shortenUrlKey, Long redirectCount) {
        this.originalUrl = originalUrl;
        this.shortenUrlKey = shortenUrlKey;
        this.redirectCount = redirectCount;
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
}