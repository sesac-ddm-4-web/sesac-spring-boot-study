package com.example.shorturl.link.domain;

import com.example.shorturl.global.exception.ApiException;
import com.example.shorturl.global.exception.ErrorCode;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Entity
@Table(name = "short_url")
public class ShortUrl {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "short_key", nullable = false, unique = true, columnDefinition = "varchar(30) collate utf8mb4_bin")
    private String key;

    @Column(nullable = false, length = 2048)
    private String originalUrl;

    @Column(length = 50)
    private String title;

    @Column(nullable = false)
    private boolean custom;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDate expiresOn;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private long visitCount;

    @Column(nullable = false)
    private long botVisitCount;

    private LocalDateTime firstVisitedAt;

    private LocalDateTime lastVisitedAt;

    @ElementCollection
    @CollectionTable(name = "short_url_daily_visit", joinColumns = @JoinColumn(name = "short_url_id"))
    @MapKeyColumn(name = "visit_date")
    @Column(name = "visit_count", nullable = false)
    private Map<LocalDate, Long> dailyVisits = new HashMap<>();

    protected ShortUrl() {
    }

    public ShortUrl(String key, String originalUrl, String title, boolean custom,
                    LocalDateTime createdAt, LocalDate expiresOn) {
        this.key = Objects.requireNonNull(key);
        this.originalUrl = Objects.requireNonNull(originalUrl);
        this.title = title == null || title.isBlank() ? null : title.strip();
        this.custom = custom;
        this.createdAt = Objects.requireNonNull(createdAt);
        this.expiresOn = expiresOn;
    }

    public String visit(LocalDateTime now, boolean bot) {
        if (!active) {
            throw new ApiException(ErrorCode.DISABLED_SHORT_URL);
        }
        if (isExpired(now.toLocalDate())) {
            throw new ApiException(ErrorCode.EXPIRED_SHORT_URL, expiresOn);
        }
        if (bot) {
            botVisitCount++;
            return originalUrl;
        }
        visitCount++;
        dailyVisits.merge(now.toLocalDate(), 1L, Long::sum);
        if (firstVisitedAt == null) {
            firstVisitedAt = now;
        }
        lastVisitedAt = now;
        return originalUrl;
    }

    public boolean isExpired(LocalDate today) {
        return expiresOn != null && today.isAfter(expiresOn);
    }

    public LinkStatus status(LocalDate today) {
        if (!active) {
            return LinkStatus.DISABLED;
        }
        return isExpired(today) ? LinkStatus.EXPIRED : LinkStatus.ACTIVE;
    }

    public void changeActive(boolean active) {
        this.active = active;
    }

    public List<DailyVisit> dailyVisits(LocalDate from, LocalDate to) {
        List<DailyVisit> result = new ArrayList<>();
        for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
            result.add(new DailyVisit(date, dailyVisits.getOrDefault(date, 0L)));
        }
        return result;
    }

    public boolean matches(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return true;
        }
        String lower = keyword.strip().toLowerCase(Locale.ROOT);
        return key.toLowerCase(Locale.ROOT).contains(lower)
                || originalUrl.toLowerCase(Locale.ROOT).contains(lower)
                || (title != null && title.toLowerCase(Locale.ROOT).contains(lower));
    }

    public Long getId() {
        return id;
    }

    public String getKey() {
        return key;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public String getTitle() {
        return title;
    }

    public boolean isCustom() {
        return custom;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDate getExpiresOn() {
        return expiresOn;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isOpened() {
        return visitCount > 0;
    }

    public long getVisitCount() {
        return visitCount;
    }

    public long getBotVisitCount() {
        return botVisitCount;
    }

    public LocalDateTime getFirstVisitedAt() {
        return firstVisitedAt;
    }

    public LocalDateTime getLastVisitedAt() {
        return lastVisitedAt;
    }
}
