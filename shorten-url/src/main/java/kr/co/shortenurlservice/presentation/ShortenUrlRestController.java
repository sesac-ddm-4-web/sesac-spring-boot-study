
package kr.co.shortenurlservice.presentation;

import jakarta.validation.Valid;
import kr.co.shortenurlservice.application.SimpleShortenUrlService;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
public class ShortenUrlRestController {

    private final SimpleShortenUrlService service;

    public ShortenUrlRestController(SimpleShortenUrlService service) {
        this.service = service;
    }

    // 단축 URL 생성
    @PostMapping("/shortenUrl")
    public ResponseEntity<ShortenUrlCreateResponseDto> createShortenUrl(
            @Valid @RequestBody ShortenUrlCreateRequestDto request
    ) {

        return ResponseEntity.ok(
                service.generateShortenUrl(request)
        );
    }

    // 단축 URL 접속
    @GetMapping("/{shortenUrlKey:[a-zA-Z0-9]{8}}")
    public ResponseEntity<Void> redirectShortenUrl(
            @PathVariable String shortenUrlKey
    ) {

        String originalUrl =
                service.getOriginalUrlByShortenUrlKey(shortenUrlKey);

        return ResponseEntity
                .status(HttpStatus.FOUND)
                .location(URI.create(originalUrl))
                .cacheControl(CacheControl.noStore())
                .build();
    }

    // 단축 URL 상세 조회
    @GetMapping("/shortenUrl/{shortenUrlKey}")
    public ResponseEntity<ShortenUrlInformationDto> getShortenUrlInformation(
            @PathVariable String shortenUrlKey
    ) {

        return ResponseEntity.ok(
                service.getShortenUrlInformationByShortenUrlKey(shortenUrlKey)
        );
    }

    // 전체 조회 + 페이지네이션 + 검색
    @GetMapping("/api/urls")
    public ResponseEntity<ShortenUrlListResponseDto> getShortenUrls(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        return ResponseEntity.ok(
                service.getShortenUrls(keyword, page, size)
        );
    }

    // 사용 중지 및 재개
    @PatchMapping("/api/urls/{shortenUrlKey}/status")
    public ResponseEntity<Void> updateStatus(
            @PathVariable String shortenUrlKey,
            @Valid @RequestBody ShortenUrlStatusRequestDto request
    ) {

        service.updateShortenUrlStatus(
                shortenUrlKey,
                request.active()
        );

        return ResponseEntity.noContent().build();
    }

    // 원본 URL 변경 (신규)
    @PatchMapping("/api/urls/{shortenUrlKey}/original-url")
    public ResponseEntity<Void> updateOriginalUrl(
            @PathVariable String shortenUrlKey,
            @Valid @RequestBody ShortenUrlUpdateRequestDto request
    ) {

        service.updateOriginalUrl(
                shortenUrlKey,
                request.originalUrl()
        );

        return ResponseEntity.noContent().build();
    }
}
