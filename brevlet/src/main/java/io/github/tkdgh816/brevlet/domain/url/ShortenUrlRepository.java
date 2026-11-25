package io.github.tkdgh816.brevlet.domain.url;

import java.util.Optional;

public interface ShortenUrlRepository {
    void save(ShortenUrl shortenUrl);

    Optional<ShortenUrl> findByKey(String shortenUrlKey);

    void increaseRedirectCount(String shortenUrlKey);
}
