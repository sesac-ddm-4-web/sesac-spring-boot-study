package com.kdongd.shortari.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "short_url")
public class ShortUrl {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "original_url", nullable = false, length = 2048)
    private String originalUrl;

    @Column(name = "short_code", nullable = false, unique = true, length = 8)
    private String shortCode;

    @Column(name = "display_name", unique = true, length = 20)
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(name = "url_mode", nullable = false, length = 20)
    private UrlMode urlMode;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 새 단축 URL 저장용 생성자(id와 createdAt은 자동으로 채워짐)
    public ShortUrl(String originalUrl, String shortCode, String displayName, UrlMode urlMode) {
        this.originalUrl = originalUrl;
        this.shortCode = shortCode;
        this.displayName = displayName;
        this.urlMode = urlMode;
    }

    // Setter : 표시 이름 변경(트랜잭션 안에서 호출하면 변경 감지로 UPDATE 실행)
    public void changeDisplayName(String displayName) {
        this.displayName = displayName;
    }
}

