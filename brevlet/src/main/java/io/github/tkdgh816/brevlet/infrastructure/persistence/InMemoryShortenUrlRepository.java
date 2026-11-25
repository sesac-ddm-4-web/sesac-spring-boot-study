package io.github.tkdgh816.brevlet.infrastructure.persistence;

import io.github.tkdgh816.brevlet.domain.key.ShortenUrlKeyConflictException;
import io.github.tkdgh816.brevlet.domain.url.ShortenUrl;
import io.github.tkdgh816.brevlet.domain.url.ShortenUrlRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Profile("test")
@Repository
public class InMemoryShortenUrlRepository implements ShortenUrlRepository {
    Map<String, ShortenUrl> shortenUrls = new ConcurrentHashMap<>();

    @Override
    public void save(ShortenUrl shortenUrl) {
        ShortenUrl existing = shortenUrls.putIfAbsent(shortenUrl.getShortenUrlKey(), shortenUrl);
        if (existing != null) {
            throw new ShortenUrlKeyConflictException("이미 존재하는 단축 URL 키입니다.");
        }
    }

    @Override
    public Optional<ShortenUrl> findByKey(String shortenUrlKey) {
        return Optional.ofNullable(shortenUrls.get(shortenUrlKey));
    }

    @Override
    public void increaseRedirectCount(String shortenUrlKey) {
        ShortenUrl shortenUrl = shortenUrls.get(shortenUrlKey);
        if (shortenUrl != null) {
            shortenUrl.increaseRedirectCount();
        }
    }
}
