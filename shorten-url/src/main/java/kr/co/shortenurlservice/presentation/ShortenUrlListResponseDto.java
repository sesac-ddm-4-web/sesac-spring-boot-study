package kr.co.shortenurlservice.presentation;

import java.util.List;

public record ShortenUrlListResponseDto(
        List<ShortenUrlItemDto> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public record ShortenUrlItemDto(
            String originalUrl,
            String shortenUrlKey,
            long redirectCount,
            boolean active
    ) {}
}