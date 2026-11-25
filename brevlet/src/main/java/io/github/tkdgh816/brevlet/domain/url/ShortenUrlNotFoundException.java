package io.github.tkdgh816.brevlet.domain.url;

import io.github.tkdgh816.brevlet.domain.ItemNotFoundException;

public class ShortenUrlNotFoundException extends ItemNotFoundException {
    public ShortenUrlNotFoundException(String message) {
        super(message);
    }
}
