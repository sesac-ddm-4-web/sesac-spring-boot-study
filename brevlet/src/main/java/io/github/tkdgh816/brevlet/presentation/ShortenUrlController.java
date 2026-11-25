package io.github.tkdgh816.brevlet.presentation;

import io.github.tkdgh816.brevlet.application.ShortenUrlService;
import io.github.tkdgh816.brevlet.domain.key.ShortenUrlKeyRule;
import io.github.tkdgh816.brevlet.dto.url.CreateShortenUrlRequest;
import io.github.tkdgh816.brevlet.dto.url.ShortenUrlResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.net.URI;

@RestController
public class ShortenUrlController {
    private final ShortenUrlService shortenUrlService;

    public ShortenUrlController(ShortenUrlService shortenUrlService) {
        this.shortenUrlService = shortenUrlService;
    }

    @GetMapping("/")
    public ModelAndView home() {
        return new ModelAndView("forward:/page/index.html");
    }

    @PostMapping("/shorten-url")
    public ResponseEntity<ShortenUrlResponse> createShortenUrl(@RequestBody @Valid CreateShortenUrlRequest request) {
        ShortenUrlResponse response = shortenUrlService.generateShortenUrl(request);
        return ResponseEntity.created(
                        URI.create("/shorten-url/" + response.shortenUrlKey())
                )
                .body(response);
    }

    @GetMapping("/{shortenUrlKey}")
    public ResponseEntity<Void> redirectShortenUrl(@PathVariable @Pattern(regexp = ShortenUrlKeyRule.KEY_REGEX) String shortenUrlKey) {
        String originalUrl = shortenUrlService.getOriginalUrlByKey(shortenUrlKey);

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(originalUrl))
                .build();
    }

    @GetMapping("/shorten-url/{shortenUrlKey}")
    public ResponseEntity<ShortenUrlResponse> getShortenUrlInfo(@PathVariable @Pattern(regexp = ShortenUrlKeyRule.KEY_REGEX) String shortenUrlKey) {
        ShortenUrlResponse response = shortenUrlService.getShortenUrlInfoByKey(shortenUrlKey);
        return ResponseEntity.ok().body(response);
    }
}
