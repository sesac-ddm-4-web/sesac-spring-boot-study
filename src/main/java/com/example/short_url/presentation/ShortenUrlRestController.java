package com.example.short_url.presentation;

import com.example.short_url.application.SimpleShortenUrlService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;

@RestController
public class ShortenUrlRestController {

    private SimpleShortenUrlService simpleShortenUrlService;

    @Autowired
    ShortenUrlRestController(SimpleShortenUrlService simpleShortenUrlService){
        this.simpleShortenUrlService = simpleShortenUrlService;
    }

    @PostMapping(value = "/shortenUrl")
    public ResponseEntity<ShortenUrlCreateResponseDto> createShortUrl(
            @Valid @RequestBody ShortenUrlCreateRequestDto shortenUrlCreateRequestDto){
        ShortenUrlCreateResponseDto shortenUrlCreateResponseDto = simpleShortenUrlService.generateShortenUrl(shortenUrlCreateRequestDto);

        return ResponseEntity.ok(shortenUrlCreateResponseDto);
    }

    @GetMapping(value = "/{shortenUrlKey}")
    public ResponseEntity<?> redirectShortenUrl(
            @PathVariable String shortenUrlKey) throws URISyntaxException {
        String originalUrl = simpleShortenUrlService.getOriginalUrlByShortenUrlKey(shortenUrlKey);

        URI redirectUri = new URI(originalUrl);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(redirectUri);


        return new ResponseEntity<>(httpHeaders, HttpStatus.MOVED_PERMANENTLY);
    }

    @GetMapping(value = "/shortenUrl/{shortenUrlKey}")
    public ResponseEntity<ShortenUrlInformationDto> getShortenUrlInformation(
            @PathVariable String shortenUrlKey)  {
        ShortenUrlInformationDto shortenUrlInformationDto = simpleShortenUrlService.getShortenUrlInformationByShortenUrlKey(shortenUrlKey);
        return ResponseEntity.ok(shortenUrlInformationDto);
    }

    @DeleteMapping(value = "/shortenUrl/{shortenUrlKey}")
    public ResponseEntity<Void> expireShortenUrl(
            @PathVariable String shortenUrlKey,
            @RequestHeader("X-Management-Token") String managementToken) {
        simpleShortenUrlService.expireShortenUrl(shortenUrlKey, managementToken);
        return ResponseEntity.noContent().build();
    }
}
