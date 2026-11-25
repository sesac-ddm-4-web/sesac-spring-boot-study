package com.kdongd.shortari.controller;

import com.kdongd.shortari.dto.request.ShortUrlCreateRequest;
import com.kdongd.shortari.dto.request.ShortUrlNameUpdateRequest;
import com.kdongd.shortari.dto.response.ShortUrlResponse;
import com.kdongd.shortari.service.ShortUrlService;

import jakarta.validation.Valid;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
public class ShortUrlController {

    private final ShortUrlService service;

    public ShortUrlController(ShortUrlService service) {
        this.service = service;
    }

    // 단축 URL 생성
    @PostMapping("/api/urls")
    public ResponseEntity<ShortUrlResponse> create(
            @Valid @RequestBody ShortUrlCreateRequest request
    ) {

        ShortUrlResponse response = service.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // 전체 링크 조회
    @GetMapping("/api/urls")
    public List<ShortUrlResponse> findAll() {
        return service.findAll();
    }

    // ID로 단건 조회
    @GetMapping("/api/urls/{id}")
    public ShortUrlResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }

    // 표시 이름 수정
    @PutMapping("/api/urls/{id}/name")
    public ShortUrlResponse updateName(
            @PathVariable Long id,
            @Valid @RequestBody ShortUrlNameUpdateRequest request
    ) {
        return service.updateName(id, request);
    }

    // 링크 삭제
    @DeleteMapping("/api/urls/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    // 단축 URL 접속 시 원본 URL로 이동
    @GetMapping("/r/{shortCode}")
    public ResponseEntity<Void> redirect(
            @PathVariable String shortCode
    ) {

        String originalUrl = service.findOriginalUrl(shortCode);

        return ResponseEntity
                .status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, URI.create(originalUrl).toString())
                .build();
    }
}