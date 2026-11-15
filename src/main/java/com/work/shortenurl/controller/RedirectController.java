package com.work.shortenurl.controller;

import com.work.shortenurl.exception.NotFoundShortenUrlException;
import com.work.shortenurl.service.ShortenUrlService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.nio.charset.StandardCharsets;

@RestController
@RequiredArgsConstructor
public class RedirectController {

    private final ShortenUrlService shortenUrlService;

    @GetMapping("/{shortenUrlKey:[a-zA-Z0-9]{8}}")
    public ResponseEntity<Void> redirect(@PathVariable String shortenUrlKey) {
        String originalUrl = shortenUrlService.getOriginalUrlForRedirect(shortenUrlKey);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(originalUrl))
                .build();
    }

    @ExceptionHandler(NotFoundShortenUrlException.class)
    public ResponseEntity<Resource> handleNotFound() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .contentType(new MediaType(MediaType.TEXT_HTML, StandardCharsets.UTF_8))
                .body(new ClassPathResource("static/404.html"));
    }
}
