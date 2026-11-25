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
    public void create(ShortenUrl shortenUrl) {
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
        shortenUrls.computeIfPresent(shortenUrlKey, (key, current) ->
                ShortenUrl.restore(
                        current.getOriginalUrl(),
                        key,
                        current.getRedirectCount() + 1,
                        current.getExpiresAt()
                ));
    }
}
