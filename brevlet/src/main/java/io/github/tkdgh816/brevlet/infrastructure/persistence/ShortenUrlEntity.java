package io.github.tkdgh816.brevlet.infrastructure.persistence;

import io.github.tkdgh816.brevlet.domain.key.ShortenUrlKeyRule;
import io.github.tkdgh816.brevlet.domain.url.ShortenUrl;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "shorten_urls",
        indexes = @Index(name = "idx_shorten_url_expires_at", columnList = "expires_at"))
class ShortenUrlEntity {
    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "shorten_url_key", length = ShortenUrlKeyRule.KEY_LENGTH, nullable = false, updatable = false)
    private String shortenUrlKey;

    @Column(name = "original_url", length = 2048, nullable = false)
    private String originalUrl;

    @Column(name = "redirect_count", nullable = false)
    private Long redirectCount;

    @Column(name = "expires_at", secondPrecision = 6)
    private LocalDateTime expiresAt;

    protected ShortenUrlEntity() {

    }

    public String getShortenUrlKey() {
        return shortenUrlKey;
    }

    public Long getRedirectCount() {
        return redirectCount;
    }

    static ShortenUrlEntity from(ShortenUrl shortenUrl){
        ShortenUrlEntity entity = new ShortenUrlEntity();
        entity.originalUrl = shortenUrl.getOriginalUrl();
        entity.shortenUrlKey = shortenUrl.getShortenUrlKey();
        entity.redirectCount = shortenUrl.getRedirectCount();
        entity.expiresAt = toLocalDateTime(shortenUrl.getExpiresAt());
        return entity;
    }

    ShortenUrl toDomain(){
        return ShortenUrl.restore(
                this.originalUrl,
                this.shortenUrlKey,
                this.redirectCount,
                toInstant(this.expiresAt)
        );
    }

    private static LocalDateTime toLocalDateTime(Instant instant) {
        return instant == null
                ? null
                : LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
    }

    private static Instant toInstant(LocalDateTime time) {
        return time == null
                ? null
                : time.toInstant(ZoneOffset.UTC);
    }
}
