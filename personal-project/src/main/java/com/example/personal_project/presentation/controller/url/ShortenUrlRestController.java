package com.example.personal_project.presentation.controller.url;

import com.example.personal_project.application.url.SimpleShortenUrlService;
import com.example.personal_project.application.url.command.CreateShortenUrlCommand;
import com.example.personal_project.presentation.dto.url.ShortenUrlCreateRequestDto;
import com.example.personal_project.presentation.dto.url.ShortenUrlCreateResponseDto;
import com.example.personal_project.presentation.dto.url.ShortenUrlInformationDto;
import jakarta.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@org.springframework.context.annotation.Profile("textbook")
public class ShortenUrlRestController {

    private final SimpleShortenUrlService simpleShortenUrlService;

    @Autowired
    public ShortenUrlRestController(SimpleShortenUrlService simpleShortenUrlService) {
        this.simpleShortenUrlService = simpleShortenUrlService;
    }

    @PostMapping("/shortenUrl")
    public ResponseEntity<ShortenUrlCreateResponseDto> createShortenUrl(
            @Valid @RequestBody ShortenUrlCreateRequestDto shortenUrlCreateRequestDto
    ) {
        ShortenUrlCreateResponseDto shortenUrlCreateResponseDto = new ShortenUrlCreateResponseDto(simpleShortenUrlService.generateShortenUrl(new CreateShortenUrlCommand(shortenUrlCreateRequestDto.getOriginalUrl(), shortenUrlCreateRequestDto.getCode())));
        return ResponseEntity.ok(shortenUrlCreateResponseDto);
    }

    @GetMapping("/study/{code}")
    public ResponseEntity<Void> redirectShortenUrl(
            @PathVariable("code") String code
    ) throws URISyntaxException {
        String originalUrl = simpleShortenUrlService.getOriginalUrlByShortenUrlKey(code);

        URI redirectUri = new URI(originalUrl);

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(redirectUri)
                .cacheControl(CacheControl.noStore())
                .build();
    }

    @GetMapping("/shortenUrl/{shortenUrlKey}")
    public ResponseEntity<ShortenUrlInformationDto> getShortenUrlInformation(
            @PathVariable String shortenUrlKey
    ) {
        ShortenUrlInformationDto shortenUrlInformationDto = new ShortenUrlInformationDto(simpleShortenUrlService.getShortenUrlInformationByShortenUrlKey(shortenUrlKey));
        return ResponseEntity.ok(shortenUrlInformationDto);
    }
}
