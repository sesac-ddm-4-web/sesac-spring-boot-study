package io.github.tkdgh816.brevlet.infrastructure.persistence;

import io.github.tkdgh816.brevlet.domain.key.ShortenUrlKeyConflictException;
import io.github.tkdgh816.brevlet.domain.url.ShortenUrl;
import io.github.tkdgh816.brevlet.domain.url.ShortenUrlNotFoundException;
import io.github.tkdgh816.brevlet.domain.url.ShortenUrlRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("h2")
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = "spring.datasource.url=jdbc:h2:mem:shorten_url_repository_test"
)
class DatabaseShortenUrlRepositoryTest {

    private static final String ORIGINAL_URL = "https://example.com/article";

    @Autowired
    private ShortenUrlRepository repository;

    @Test
    @DisplayName("중복 키 생성이 실패해도 기존 목적지, 만료 시각, 조회 수가 보존된다")
    void duplicateKeyPreservesExistingLink() {
        String key = "AbCd2345";
        Instant expiresAt = Instant.parse("2030-05-01T12:34:56Z");
        repository.create(new ShortenUrl(ORIGINAL_URL, key, expiresAt));
        repository.increaseRedirectCount(key);

        var duplicate = new ShortenUrl("https://example.com/other", key, null);
        assertThatThrownBy(() -> repository.create(duplicate))
                .isInstanceOf(ShortenUrlKeyConflictException.class);

        ShortenUrl saved = repository.findByKey(key).orElseThrow();
        assertThat(saved.getOriginalUrl()).isEqualTo(ORIGINAL_URL);
        assertThat(saved.getExpiresAt()).isEqualTo(expiresAt);
        assertThat(saved.getRedirectCount()).isEqualTo(1L);
    }

    @Test
    @DisplayName("만료 시각을 UTC로 저장하고 마이크로초까지 같은 Instant로 조회한다")
    void persistsAndRestoresUtcExpiration(@Autowired JdbcTemplate jdbc) {
        String key = "BcDe3456";
        Instant expiresAt = Instant.parse("2030-12-31T23:59:59.123456Z");
        repository.create(new ShortenUrl(ORIGINAL_URL, key, expiresAt));

        LocalDateTime stored = jdbc.queryForObject(
                "select expires_at from shorten_urls where shorten_url_key = ?",
                LocalDateTime.class, key);

        assertThat(stored).isEqualTo(LocalDateTime.ofInstant(expiresAt, ZoneOffset.UTC));
        assertThat(repository.findByKey(key).orElseThrow().getExpiresAt()).isEqualTo(expiresAt);
    }

    @Test
    @DisplayName("영구 링크는 다시 조회해도 만료 시각이 null이다")
    void permanentLinkHasNoExpiration() {
        String key = "CdEf4567";
        repository.create(new ShortenUrl(ORIGINAL_URL, key, null));

        assertThat(repository.findByKey(key).orElseThrow().getExpiresAt()).isNull();
    }

    @Test
    @DisplayName("없는 키를 조회하면 빈 Optional을 반환한다")
    void missingKeyReturnsEmpty() {
        assertThat(repository.findByKey("DeFg5678")).isEmpty();
    }

    @Test
    @DisplayName("조회 수는 지정한 링크만 호출 횟수만큼 증가한다")
    void incrementsOnlyTheRequestedLink() {
        String targetKey = "EfGh6789";
        String otherKey = "FgHj7892";
        repository.create(new ShortenUrl(ORIGINAL_URL, targetKey, null));
        repository.create(new ShortenUrl(ORIGINAL_URL, otherKey, null));

        repository.increaseRedirectCount(targetKey);
        repository.increaseRedirectCount(targetKey);

        assertThat(repository.findByKey(targetKey).orElseThrow().getRedirectCount()).isEqualTo(2L);
        assertThat(repository.findByKey(otherKey).orElseThrow().getRedirectCount()).isZero();
    }

    @Test
    @DisplayName("없는 키의 조회 수를 증가시키면 링크 없음 예외가 발생한다")
    void incrementingMissingKeyThrowsNotFound() {
        assertThatThrownBy(() -> repository.increaseRedirectCount("GhJk8923"))
                .isInstanceOf(ShortenUrlNotFoundException.class);
    }
}
