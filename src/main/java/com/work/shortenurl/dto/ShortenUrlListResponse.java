package com.work.shortenurl.dto;

import com.work.shortenurl.domain.ShortenUrl;

import java.util.List;

public record ShortenUrlListResponse(
        long totalRedirectCount,
        List<ShortenUrlInformation> items
) {
    public static ShortenUrlListResponse of(List<ShortenUrl> shortenUrls, String baseUrl) {
        List<ShortenUrlInformation> items = shortenUrls.stream()
                .map(shortenUrl -> ShortenUrlInformation.of(shortenUrl, baseUrl))
                .toList();
        long total = items.stream()
                .mapToLong(ShortenUrlInformation::redirectCount)
                .sum();
        return new ShortenUrlListResponse(total, items);
    }}
