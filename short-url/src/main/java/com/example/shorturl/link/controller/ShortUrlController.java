package com.example.shorturl.link.controller;

import com.example.shorturl.link.domain.LinkStatus;
import com.example.shorturl.link.dto.req.ShortUrlCreateReq;
import com.example.shorturl.link.dto.req.ShortUrlStatusReq;
import com.example.shorturl.link.dto.res.ShortUrlRes;
import com.example.shorturl.link.dto.res.ShortUrlStatsRes;
import com.example.shorturl.link.service.ShortUrlService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/short-urls")
public class ShortUrlController {

    private final ShortUrlService shortUrlService;

    public ShortUrlController(ShortUrlService shortUrlService) {
        this.shortUrlService = shortUrlService;
    }

    @PostMapping
    public ResponseEntity<ShortUrlRes> createShortUrl(@Valid @RequestBody ShortUrlCreateReq request) {
        URI serviceOrigin = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUri();
        ShortUrlRes created = shortUrlService.create(request, serviceOrigin);
        // 원본 주소가 아니라 만든 단축 URL 정보 주소
        return ResponseEntity.created(URI.create("/api/short-urls/" + created.key())).body(created);
    }

    @GetMapping
    public List<ShortUrlRes> searchShortUrls(@RequestParam(required = false) String q,
                                             @RequestParam(required = false) LinkStatus status,
                                             @RequestParam(required = false) Boolean opened) {
        return shortUrlService.search(q, status, opened);
    }

    @GetMapping("/{key}")
    public ShortUrlRes findShortUrl(@PathVariable String key) {
        return shortUrlService.findByKey(key);
    }

    @PatchMapping("/{key}/status")
    public ShortUrlRes changeStatus(@PathVariable String key, @Valid @RequestBody ShortUrlStatusReq request) {
        return shortUrlService.changeActive(key, request.active());
    }

    @GetMapping("/{key}/stats")
    public ShortUrlStatsRes findStats(@PathVariable String key, @RequestParam(defaultValue = "7") int days) {
        return shortUrlService.stats(key, days);
    }
}
