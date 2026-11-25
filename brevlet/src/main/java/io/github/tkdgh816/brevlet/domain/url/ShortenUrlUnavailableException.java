package io.github.tkdgh816.brevlet.domain.url;

public class ShortenUrlUnavailableException extends RuntimeException {
    public ShortenUrlUnavailableException(String message) {
        super(message);
    }
}
