package com.work.shortenurl.controller;

import com.work.shortenurl.dto.ShortenUrlCreateRequest;
import com.work.shortenurl.dto.ShortenUrlCreateResponse;
import com.work.shortenurl.dto.ShortenUrlInformation;
import com.work.shortenurl.dto.ShortenUrlListResponse;
import com.work.shortenurl.service.ShortenUrlService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/shorten-urls")
@RequiredArgsConstructor
public class ShortenUrlController {

    private final ShortenUrlService shortenUrlService;

    @PostMapping
    public ResponseEntity<ShortenUrlCreateResponse> createShortenUrl(
            @Valid @RequestBody ShortenUrlCreateRequest request
            ) {
        ShortenUrlCreateResponse response = shortenUrlService.createShortenUrl(request);
        URI location = URI.create("/api/shorten-urls/" + response.shortenUrlKey());
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{shortenUrlKey}")
    public ResponseEntity<ShortenUrlInformation> getShortenUrlInformation(
            @PathVariable String shortenUrlKey
    ) {
        return ResponseEntity.ok(shortenUrlService.getShortenUrlInformation(shortenUrlKey));
    }

    @GetMapping
    public ResponseEntity<ShortenUrlListResponse> getShortenUrls(
            @RequestParam(required = false) String originalUrl
    ) {
        return ResponseEntity.ok(shortenUrlService.getShortenUrls(originalUrl));
    }
}
