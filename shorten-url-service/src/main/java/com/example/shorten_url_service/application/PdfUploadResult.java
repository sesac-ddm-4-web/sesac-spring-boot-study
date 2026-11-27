package com.example.shorten_url_service.application;

import com.example.shorten_url_service.domain.ShortenUrl;

public record PdfUploadResult(ShortenUrl shortenUrl, String cancelToken) {
}