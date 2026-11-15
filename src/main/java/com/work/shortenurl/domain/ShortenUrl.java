package com.work.shortenurl.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "shorten_url",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_shorten_url_key",
                columnNames = "shorten_url_key"
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShortenUrl {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(nullable = false, length = 2048)
        private String originalUrl;

        @Column(nullable = false, length = 8)
        private String shortenUrlKey;

        @Column(nullable = false, length = 50)
        private String channel;

        @Column(nullable = false)
        private long redirectCount;

        @Column(nullable = false, updatable = false)
        private LocalDateTime createdAt;

        private LocalDateTime lastRedirectedAt;

        public ShortenUrl(String originalUrl, String shortenUrlKey, String channel) {
                this.originalUrl = originalUrl;
                this.shortenUrlKey = shortenUrlKey;
                this.channel = channel.trim().toLowerCase();
                this.redirectCount = 0;
                this.createdAt = LocalDateTime.now();
        }

}
