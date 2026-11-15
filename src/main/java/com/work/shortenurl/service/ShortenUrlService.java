package com.work.shortenurl.service;

import com.work.shortenurl.config.AppProperties;
import com.work.shortenurl.domain.ShortenUrl;
import com.work.shortenurl.dto.ShortenUrlCreateRequest;
import com.work.shortenurl.dto.ShortenUrlCreateResponse;
import com.work.shortenurl.dto.ShortenUrlInformation;
import com.work.shortenurl.dto.ShortenUrlListResponse;
import com.work.shortenurl.exception.LackOfShortenUrlKeyException;
import com.work.shortenurl.exception.NotFoundShortenUrlException;
import com.work.shortenurl.keygen.KeyGenerator;
import com.work.shortenurl.repository.ShortenUrlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShortenUrlService {

    private static final int MAX_RETRY = 5;

    private final ShortenUrlRepository shortenUrlRepository;
    private final KeyGenerator keyGenerator;
    private final AppProperties appProperties;
    private final UrlValidator urlValidator;

    // 단축 URL 생성
    @Transactional
    public ShortenUrlCreateResponse createShortenUrl(ShortenUrlCreateRequest request) {
        String originalUrl = request.originalUrl().trim();
        urlValidator.validate(originalUrl);

        String shortenUrlKey = generateUniqueKey();
        ShortenUrl shortenUrl = new ShortenUrl(
                request.originalUrl().trim(),
                shortenUrlKey,
                request.channel()
        );
        ShortenUrl saved = shortenUrlRepository.save(shortenUrl);
        return ShortenUrlCreateResponse.of(saved, appProperties.baseUrl());
    }

    // URL 정보 조회
    public ShortenUrlInformation getShortenUrlInformation(String shortenUrlKey) {
        ShortenUrl shortenUrl = shortenUrlRepository.findByShortenUrlKey(shortenUrlKey)
                .orElseThrow(() -> new NotFoundShortenUrlException(shortenUrlKey));
        return ShortenUrlInformation.of(shortenUrl, appProperties.baseUrl());
    }

    // redirect
    @Transactional
    public String getOriginalUrlForRedirect(String shortenUrlKey) {
        ShortenUrl shortenUrl = shortenUrlRepository.findByShortenUrlKey(shortenUrlKey)
                .orElseThrow(() -> new NotFoundShortenUrlException(shortenUrlKey));
        shortenUrlRepository.increaseRedirectCount(shortenUrlKey, LocalDateTime.now());
        return shortenUrl.getOriginalUrl();
    }

    public ShortenUrlListResponse getShortenUrls(String originalUrl) {
        List<ShortenUrl> shortenUrls = (originalUrl == null || originalUrl.isBlank())
                ? shortenUrlRepository.findAllOrderByCreatedAtDesc()
                : shortenUrlRepository.findAllByOriginalUrlOrderByRedirectCountDesc(originalUrl.trim());
        return ShortenUrlListResponse.of(shortenUrls, appProperties.baseUrl());
    }

    private String generateUniqueKey() {
        for (int i = 0; i < MAX_RETRY; i++) {
            String key = keyGenerator.generate();
            if (!shortenUrlRepository.existsByShortenUrlKey(key)) {
                return key;
            }
        }
        throw new LackOfShortenUrlKeyException();
    }
}
