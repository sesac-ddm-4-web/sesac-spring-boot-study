package com.example.shorturl.link.domain;

import com.example.shorturl.global.exception.ApiException;
import com.example.shorturl.global.exception.ErrorCode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class ShortUrl {

    private final String key;
    private final String originalUrl;
    private final String title;
    private final boolean custom;
    private final LocalDateTime createdAt;
    private final LocalDate expiresOn;

    private boolean active = true;
    private long visitCount;
    private long botVisitCount;
    private LocalDateTime firstVisitedAt;
    private LocalDateTime lastVisitedAt;
    private final Map<LocalDate, Long> dailyVisits = new HashMap<>();

    public ShortUrl(String key, String originalUrl, String title, boolean custom,
                    LocalDateTime createdAt, LocalDate expiresOn) {
        this.key = Objects.requireNonNull(key);
        this.originalUrl = Objects.requireNonNull(originalUrl);
        this.title = title == null || title.isBlank() ? null : title.strip();
        this.custom = custom;
        this.createdAt = Objects.requireNonNull(createdAt);
        this.expiresOn = expiresOn;
    }

    // synchronized로 동시 방문 때 방문 수가 빠지는 것을 막음
    public synchronized String visit(LocalDateTime now, boolean bot) {
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

    public synchronized LinkStatus status(LocalDate today) {
        if (!active) {
            return LinkStatus.DISABLED;
        }
        return isExpired(today) ? LinkStatus.EXPIRED : LinkStatus.ACTIVE;
    }

    public synchronized void changeActive(boolean active) {
        this.active = active;
    }

    public synchronized List<DailyVisit> dailyVisits(LocalDate from, LocalDate to) {
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

    public synchronized boolean isActive() {
        return active;
    }

    public synchronized boolean isOpened() {
        return visitCount > 0;
    }

    public synchronized long getVisitCount() {
        return visitCount;
    }

    public synchronized long getBotVisitCount() {
        return botVisitCount;
    }

    public synchronized LocalDateTime getFirstVisitedAt() {
        return firstVisitedAt;
    }

    public synchronized LocalDateTime getLastVisitedAt() {
        return lastVisitedAt;
    }
}
