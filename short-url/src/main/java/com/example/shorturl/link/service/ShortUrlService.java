package com.example.shorturl.link.service;

import com.example.shorturl.global.exception.ApiException;
import com.example.shorturl.global.exception.ErrorCode;
import com.example.shorturl.link.domain.BotDetector;
import com.example.shorturl.link.domain.DailyVisit;
import com.example.shorturl.link.domain.LinkStatus;
import com.example.shorturl.link.domain.ShortUrl;
import com.example.shorturl.link.domain.ShortUrlKeyGenerator;
import com.example.shorturl.link.domain.UrlPolicy;
import com.example.shorturl.link.dto.req.ShortUrlCreateReq;
import com.example.shorturl.link.dto.res.ShortUrlRes;
import com.example.shorturl.link.dto.res.ShortUrlStatsRes;
import com.example.shorturl.link.repository.ShortUrlRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ShortUrlService {

    public static final int MAX_KEY_RETRY = 5;
    public static final int MAX_STATS_DAYS = 30;

    private final ShortUrlRepository shortUrlRepository;
    private final ShortUrlKeyGenerator keyGenerator;
    private final Clock clock;

    public ShortUrlService(ShortUrlRepository shortUrlRepository, ShortUrlKeyGenerator keyGenerator, Clock clock) {
        this.shortUrlRepository = shortUrlRepository;
        this.keyGenerator = keyGenerator;
        this.clock = clock;
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public ShortUrlRes create(ShortUrlCreateReq request, URI serviceOrigin) {
        String originalUrl = UrlPolicy.normalizeOriginalUrl(request.originalUrl());
        UrlPolicy.validateNotSelfLink(originalUrl, serviceOrigin);
        LocalDateTime now = LocalDateTime.now(clock);
        if (request.expiresOn() != null && request.expiresOn().isBefore(now.toLocalDate())) {
            throw new ApiException(ErrorCode.PAST_EXPIRATION_DATE);
        }

        ShortUrl saved = hasCustomKey(request)
                ? saveWithCustomKey(request, originalUrl, now)
                : saveWithGeneratedKey(request, originalUrl, now);
        return ShortUrlRes.of(saved, now.toLocalDate());
    }

    @Transactional
    public String redirect(String key, String userAgent, boolean headRequest) {
        boolean bot = headRequest || BotDetector.isBot(userAgent);
        ShortUrl shortUrl = shortUrlRepository.findByKeyForUpdate(key)
                .orElseThrow(() -> new ApiException(ErrorCode.SHORT_URL_NOT_FOUND, key));
        return shortUrl.visit(LocalDateTime.now(clock), bot);
    }

    public ShortUrlRes findByKey(String key) {
        return ShortUrlRes.of(findShortUrl(key), today());
    }

    public List<ShortUrlRes> search(String keyword, LinkStatus status, Boolean opened) {
        LocalDate today = today();
        return shortUrlRepository.findAll().stream()
                .filter(shortUrl -> shortUrl.matches(keyword))
                .filter(shortUrl -> status == null || shortUrl.status(today) == status)
                .filter(shortUrl -> opened == null || shortUrl.isOpened() == opened)
                .map(shortUrl -> ShortUrlRes.of(shortUrl, today))
                .toList();
    }

    @Transactional
    public ShortUrlRes changeActive(String key, boolean active) {
        ShortUrl shortUrl = findShortUrl(key);
        shortUrl.changeActive(active);
        return ShortUrlRes.of(shortUrl, today());
    }

    public ShortUrlStatsRes stats(String key, int days) {
        if (days < 1 || days > MAX_STATS_DAYS) {
            throw new ApiException(ErrorCode.INVALID_STATS_DAYS, MAX_STATS_DAYS);
        }
        ShortUrl shortUrl = findShortUrl(key);
        LocalDate to = today();
        LocalDate from = to.minusDays(days - 1L);
        List<DailyVisit> daily = shortUrl.dailyVisits(from, to);
        long periodCount = daily.stream().mapToLong(DailyVisit::count).sum();
        return new ShortUrlStatsRes(key, shortUrl.getVisitCount(), shortUrl.getBotVisitCount(),
                periodCount, from, to, daily);
    }

    private boolean hasCustomKey(ShortUrlCreateReq request) {
        return request.customKey() != null && !request.customKey().isBlank();
    }

    private ShortUrl saveWithCustomKey(ShortUrlCreateReq request, String originalUrl, LocalDateTime now) {
        String key = UrlPolicy.validateCustomKey(request.customKey());
        ShortUrl shortUrl = new ShortUrl(key, originalUrl, request.title(), true, now, request.expiresOn());
        if (!shortUrlRepository.saveIfAbsent(shortUrl)) {
            throw new ApiException(ErrorCode.DUPLICATE_CUSTOM_KEY, key);
        }
        return shortUrl;
    }

    private ShortUrl saveWithGeneratedKey(ShortUrlCreateReq request, String originalUrl, LocalDateTime now) {
        for (int attempt = 0; attempt < MAX_KEY_RETRY; attempt++) {
            ShortUrl shortUrl = new ShortUrl(keyGenerator.generate(), originalUrl, request.title(), false,
                    now, request.expiresOn());
            if (shortUrlRepository.saveIfAbsent(shortUrl)) {
                return shortUrl;
            }
        }
        throw new ApiException(ErrorCode.LACK_OF_SHORT_URL_KEY);
    }

    private ShortUrl findShortUrl(String key) {
        return shortUrlRepository.findByKey(key)
                .orElseThrow(() -> new ApiException(ErrorCode.SHORT_URL_NOT_FOUND, key));
    }

    private LocalDate today() {
        return LocalDate.now(clock);
    }
}
