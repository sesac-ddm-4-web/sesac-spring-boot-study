package io.github.tkdgh816.brevlet.presentation;

import io.github.tkdgh816.brevlet.domain.ItemNotFoundException;
import io.github.tkdgh816.brevlet.domain.key.KeyGenerationFailedException;
import io.github.tkdgh816.brevlet.domain.url.ShortenUrlUnavailableException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ItemNotFoundException.class)
    public ResponseEntity<String> handleItemNotFoundException(ItemNotFoundException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(KeyGenerationFailedException.class)
    public ResponseEntity<String> handleKeyGenerationFailedException(KeyGenerationFailedException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private static final String EXPIRED_PAGE = getExpiredPage();

    private static String getExpiredPage() {
        try {
            return new ClassPathResource("static/page/expired.html").getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("만료 페이지를 찾을 수 없습니다.", e);
        }
    }

    @ExceptionHandler(ShortenUrlUnavailableException.class)
    public ResponseEntity<String> handleShortenUrlExpiredException(ShortenUrlUnavailableException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.GONE)
                .contentType(MediaType.TEXT_HTML)
                .body(EXPIRED_PAGE);
    }
}
