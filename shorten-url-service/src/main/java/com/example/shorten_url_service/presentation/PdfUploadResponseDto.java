package com.example.shorten_url_service.presentation;

public record PdfUploadResponseDto(
        String originalUrl,
        String shortenUrlKey,
        String cancelToken
) {
}
