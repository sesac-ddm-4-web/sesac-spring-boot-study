package com.example.shorturl.link.controller;

import com.example.shorturl.link.service.ShortUrlService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
public class RedirectController {

    private final ShortUrlService shortUrlService;

    public RedirectController(ShortUrlService shortUrlService) {
        this.shortUrlService = shortUrlService;
    }

    // 점(.)을 빼서 index.html 같은 정적 파일과 안 겹치게 함
    @GetMapping("/{key:[A-Za-z0-9_-]{4,30}}")
    public ResponseEntity<Void> redirect(@PathVariable String key,
                                         @RequestHeader(value = HttpHeaders.USER_AGENT, required = false) String userAgent,
                                         HttpServletRequest request) {
        // HEAD는 링크 검사기가 주로 보내는 요청이라 봇으로 셈
        boolean headRequest = HttpMethod.HEAD.matches(request.getMethod());
        // 봇도 이동시켜야 카톡 미리보기가 만들어짐
        String originalUrl = shortUrlService.redirect(key, userAgent, headRequest);
        // 301은 브라우저가 캐시해서 방문 수가 안 올라감
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(originalUrl))
                .cacheControl(CacheControl.noStore())
                .build();
    }
}
