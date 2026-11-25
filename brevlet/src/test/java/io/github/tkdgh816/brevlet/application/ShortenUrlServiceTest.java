package io.github.tkdgh816.brevlet.application;

import io.github.tkdgh816.brevlet.domain.key.KeyGenerationFailedException;
import io.github.tkdgh816.brevlet.domain.key.ShortenUrlKeyGenerator;
import io.github.tkdgh816.brevlet.domain.url.ShortenUrlNotFoundException;
import io.github.tkdgh816.brevlet.domain.url.ShortenUrlUnavailableException;
import io.github.tkdgh816.brevlet.dto.url.CreateShortenUrlRequest;
import io.github.tkdgh816.brevlet.dto.url.ShortenUrlResponse;
import io.github.tkdgh816.brevlet.infrastructure.keygenerator.RandomShortenUrlKeyGenerator;
import io.github.tkdgh816.brevlet.infrastructure.persistence.InMemoryShortenUrlRepository;
import io.github.tkdgh816.brevlet.support.MutableClock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShortenUrlServiceTest {
    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");
    private static final String ORIGINAL_URL = "https://example.com/some/long/path";

    private InMemoryShortenUrlRepository repository;
    private MutableClock clock;
    private ShortenUrlService service;

    @BeforeEach
    void setUp() {
        repository = new InMemoryShortenUrlRepository();
        clock = new MutableClock(NOW);
        service = new ShortenUrlService(repository, new RandomShortenUrlKeyGenerator(), clock);
    }

    private ShortenUrlService serviceWithKeys(String... keys) {
        Iterator<String> iterator = List.of(keys).iterator();
        ShortenUrlKeyGenerator generator = iterator::next;
        return new ShortenUrlService(repository, generator, clock);
    }

    private String create(Integer expiresInDays) {
        return service.generateShortenUrl(new CreateShortenUrlRequest(ORIGINAL_URL, expiresInDays)).shortenUrlKey();
    }

    @Test
    @DisplayName("만료 기간을 지정하지 않으면 expiresAt은 null이다")
    void generate_withoutExpiresInDays_setsExpiresAtNull() {
        ShortenUrlResponse response = service.generateShortenUrl(new CreateShortenUrlRequest(ORIGINAL_URL, null));

        assertThat(response.expiresAt()).isNull();
        assertThat(response.redirectCount()).isZero();
    }

    @Test
    @DisplayName("만료 기간을 지정하면 Clock 기준 now + N일이 만료 시각이 된다")
    void generate_withExpiresInDays_setsExpiresAtFromClock() {
        ShortenUrlResponse response = service.generateShortenUrl(new CreateShortenUrlRequest(ORIGINAL_URL, 7));

        assertThat(response.expiresAt()).isEqualTo(NOW.plus(Duration.ofDays(7)));
    }

    @Test
    @DisplayName("영구 링크는 시간이 얼마나 지나도 리다이렉트된다")
    void getOriginalUrl_permanentLink_availableFarInFuture() {
        String key = create(null);
        clock.advance(Duration.ofDays(365 * 100));

        assertThat(service.getOriginalUrlByKey(key)).isEqualTo(ORIGINAL_URL);
    }

    @Test
    @DisplayName("만료 직전에는 원본 URL을 돌려준다")
    void getOriginalUrl_justBeforeExpiresAt_returnsOriginalUrl() {
        String key = create(1);
        clock.setInstant(NOW.plus(Duration.ofDays(1)).minusNanos(1));

        assertThat(service.getOriginalUrlByKey(key)).isEqualTo(ORIGINAL_URL);
    }

    @Test
    @DisplayName("정확히 만료 시각이 되면 사용할 수 없다")
    void getOriginalUrl_atExactExpiresAt_throwsUnavailable() {
        String key = create(1);
        clock.setInstant(NOW.plus(Duration.ofDays(1)));

        assertThatThrownBy(() -> service.getOriginalUrlByKey(key))
                .isInstanceOf(ShortenUrlUnavailableException.class);
    }

    @Test
    @DisplayName("만료된 링크는 사용할 수 없다")
    void getOriginalUrl_expiredLink_throwsUnavailable() {
        String key = create(1);
        clock.advance(Duration.ofDays(2));

        assertThatThrownBy(() -> service.getOriginalUrlByKey(key))
                .isInstanceOf(ShortenUrlUnavailableException.class);
    }

    @Test
    @DisplayName("리다이렉트할 때마다 횟수가 증가한다")
    void getOriginalUrl_availableLink_increasesRedirectCount() {
        String key = create(null);

        service.getOriginalUrlByKey(key);
        service.getOriginalUrlByKey(key);

        assertThat(service.getShortenUrlInfoByKey(key).redirectCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("만료된 링크는 리다이렉트 횟수가 증가하지 않는다")
    void getOriginalUrl_expiredLink_doesNotIncreaseRedirectCount() {
        String key = create(1);
        clock.advance(Duration.ofDays(2));

        assertThatThrownBy(() -> service.getOriginalUrlByKey(key))
                .isInstanceOf(ShortenUrlUnavailableException.class);
        assertThat(service.getShortenUrlInfoByKey(key).redirectCount()).isZero();
    }

    @Test
    @DisplayName("존재하지 않는 키는 NotFound 예외가 발생한다")
    void getOriginalUrl_unknownKey_throwsNotFound() {
        assertThatThrownBy(() -> service.getOriginalUrlByKey("AbCd2345"))
                .isInstanceOf(ShortenUrlNotFoundException.class);
    }

    @Test
    @DisplayName("존재하지 않는 키의 정보 조회는 NotFound 예외가 발생한다")
    void getInfo_unknownKey_throwsNotFound() {
        assertThatThrownBy(() -> service.getShortenUrlInfoByKey("AbCd2345"))
                .isInstanceOf(ShortenUrlNotFoundException.class);
    }

    @Test
    @DisplayName("만료된 링크도 정보 조회는 가능하고 expiresAt이 내려온다")
    void getInfo_expiredLink_returnsInfoWithExpiresAt() {
        String key = create(1);
        clock.advance(Duration.ofDays(2));

        ShortenUrlResponse info = service.getShortenUrlInfoByKey(key);

        assertThat(info.shortenUrlKey()).isEqualTo(key);
        assertThat(info.expiresAt()).isEqualTo(NOW.plus(Duration.ofDays(1)));
    }

    @Test
    @DisplayName("만료된 키가 다시 생성되면 재시도해서 새 키를 발급한다")
    void generate_whenGeneratorReturnsExpiredKey_retriesWithNewKey() {
        ShortenUrlService keyed = serviceWithKeys("AAAAAAAA", "AAAAAAAA", "BBBBBBBB");
        keyed.generateShortenUrl(new CreateShortenUrlRequest(ORIGINAL_URL, 1));
        clock.advance(Duration.ofDays(2));

        ShortenUrlResponse second = keyed.generateShortenUrl(new CreateShortenUrlRequest("https://other.com", null));

        assertThat(second.shortenUrlKey()).isEqualTo("BBBBBBBB");
        assertThat(keyed.getShortenUrlInfoByKey("AAAAAAAA").originalUrl()).isEqualTo(ORIGINAL_URL);
    }

    @Test
    @DisplayName("모든 시도에서 키가 충돌하면 키 생성 실패 예외가 발생한다")
    void generate_whenAllAttemptsCollide_throwsKeyGenerationFailed() {
        ShortenUrlService keyed = serviceWithKeys("AAAAAAAA", "AAAAAAAA", "AAAAAAAA", "AAAAAAAA", "AAAAAAAA", "AAAAAAAA");
        keyed.generateShortenUrl(new CreateShortenUrlRequest(ORIGINAL_URL, null));

        assertThatThrownBy(() -> keyed.generateShortenUrl(new CreateShortenUrlRequest(ORIGINAL_URL, null)))
                .isInstanceOf(KeyGenerationFailedException.class);
    }
}
