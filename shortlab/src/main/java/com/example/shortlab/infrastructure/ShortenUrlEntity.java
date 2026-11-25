package com.example.shortlab.infrastructure;

import jakarta.persistence.*;

import java.time.LocalDateTime;

// 테이블명과 매핑
@Entity
@Table(name = "shorten_url")
public class ShortenUrlEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shorten_url_key", nullable = false, unique = true, length = 32)
    private String shortenUrlKey;

    @Column(name = "original_url", nullable = false, length = 2048)
    private String originalUrl;

    @Column(name = "redirect_count", nullable = false)
    private Long redirectCount = 0L;

    /// 링크가 언제 만들어졌는 지 기록하는 필드
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected ShortenUrlEntity() {}

    public ShortenUrlEntity(String shortenUrlKey, String originalUrl){
        this.shortenUrlKey = shortenUrlKey;
        this.originalUrl = originalUrl;
        this.createdAt = LocalDateTime.now();
    }

    public String getShortenUrlKey() {
        return shortenUrlKey;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public long getRedirectCount() {
        return redirectCount;
    }
}
