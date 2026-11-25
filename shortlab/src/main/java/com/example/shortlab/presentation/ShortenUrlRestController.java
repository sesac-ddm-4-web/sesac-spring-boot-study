package com.example.shortlab.presentation;

import com.example.shortlab.application.QrCodeService;
import com.example.shortlab.application.SimpleShortenUrlService;
import com.example.shortlab.presentation.dto.ClickLogPageResponseDto;
import com.example.shortlab.presentation.dto.ShortenUrlCreateRequestDto;
import com.example.shortlab.presentation.dto.ShortenUrlCreateResponseDto;
import com.example.shortlab.presentation.dto.ShortenUrlInformationDto;
import com.google.zxing.qrcode.QRCodeWriter;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
public class ShortenUrlRestController {

    private SimpleShortenUrlService simpleShortenUrlService;
    private QrCodeService qrCodeService;

    @Autowired
    public void setSimpleShortenUrlService(SimpleShortenUrlService simpleShortenUrlService, QrCodeService qrCodeService) {
        this.simpleShortenUrlService = simpleShortenUrlService;
        this.qrCodeService = qrCodeService;

    }

    // 단축 url 생성
    @PostMapping(value = "/shortenUrl")
    public ResponseEntity<ShortenUrlCreateResponseDto> createShortenUrl(
            @Valid @RequestBody ShortenUrlCreateRequestDto shortenUrlCreateRequestDto
    ){
        ShortenUrlCreateResponseDto shortenUrlCreateResponseDto =
                simpleShortenUrlService.generateShortenUrl(shortenUrlCreateRequestDto);
        return ResponseEntity.ok(shortenUrlCreateResponseDto);
    }

    // 리다이렉트, 웹 브라우저 <-> 서버
    @GetMapping("/{shortenUrlKey:[A-Za-z0-9-]+}")
    public ResponseEntity<?> redirectShortenUrl(
            @PathVariable String shortenUrlKey
    ) throws URISyntaxException {
        String originalUrl = simpleShortenUrlService.getOriginalUrlByShortenUrlKey(shortenUrlKey);

        URI redirectUri = new URI(originalUrl);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(redirectUri);

//        return new ResponseEntity<>(httpHeaders, HttpStatus.MOVED_PERMANENTLY);
        return new ResponseEntity<>(httpHeaders, HttpStatus.FOUND);
    }

    // 통계
    @GetMapping(value = "/shortenUrl/{shortenUrlKey}")
    public ResponseEntity<ShortenUrlInformationDto> getShortenUrlInformation(
            @PathVariable String shortenUrlKey
    ){
        ShortenUrlInformationDto shortenUrlInformationDto =
                simpleShortenUrlService.getShortenUrlInforamtionByShortenUrlKey(shortenUrlKey);
        return ResponseEntity.ok(shortenUrlInformationDto);
    }

    // 클릭 로그
    @GetMapping("/shortenUrl/{shortenUrlKey}/clicks")
    public ResponseEntity<ClickLogPageResponseDto> getClicks(@PathVariable String shortenUrlKey,
                                                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                                                             @RequestParam(required = false) Long beforeId,
                                                             @RequestParam(defaultValue = "20") int size) {
        if (from != null && to != null && from.isAfter(to)) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(simpleShortenUrlService.getClicks(shortenUrlKey, from, to, beforeId, size));
    }
    // QR 앤드포인트
    @GetMapping("/shortenUrl/{shortenUrlKey}/qr")
    public ResponseEntity<String> qr (@PathVariable String shortenUrlKey) {
        simpleShortenUrlService.getShortenUrlInforamtionByShortenUrlKey(shortenUrlKey);
        String shortUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/{key}").buildAndExpand(shortenUrlKey).toString();
        return ResponseEntity.ok()
                .contentType(new MediaType("image", "svg+xml", StandardCharsets.UTF_8))
                .body(qrCodeService.generateSvg(shortUrl));
    }
}
