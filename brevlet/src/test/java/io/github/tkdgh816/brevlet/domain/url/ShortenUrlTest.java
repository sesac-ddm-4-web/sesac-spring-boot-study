package io.github.tkdgh816.brevlet.domain.url;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class ShortenUrlTest {
    private static final Instant EXPIRES_AT = Instant.parse("2026-01-08T00:00:00Z");

    private static ShortenUrl expiringUrl() {
        return new ShortenUrl("https://example.com", "AbCd2345", EXPIRES_AT);
    }

    @Test
    @DisplayName("만료 시각이 없으면 아주 먼 미래에도 사용 가능하다")
    void isAvailable_withoutExpiration_returnsTrue() {
        ShortenUrl permanent = new ShortenUrl("https://example.com", "AbCd2345", null);

        assertThat(permanent.isAvailable(Instant.parse("9999-12-31T23:59:59Z"))).isTrue();
    }

    @Test
    @DisplayName("만료 시각 직전에는 사용 가능하다")
    void isAvailable_beforeExpiresAt_returnsTrue() {
        assertThat(expiringUrl().isAvailable(EXPIRES_AT.minus(Duration.ofNanos(1)))).isTrue();
    }

    @Test
    @DisplayName("정확히 만료 시각인 순간에는 사용할 수 없다")
    void isAvailable_atExactExpiresAt_returnsFalse() {
        assertThat(expiringUrl().isAvailable(EXPIRES_AT)).isFalse();
    }

    @Test
    @DisplayName("만료 시각 이후에는 사용할 수 없다")
    void isAvailable_afterExpiresAt_returnsFalse() {
        assertThat(expiringUrl().isAvailable(EXPIRES_AT.plus(Duration.ofNanos(1)))).isFalse();
    }

    @Test
    @DisplayName("새로 만든 링크의 리다이렉트 횟수는 0이다")
    void constructor_newUrl_startsWithZeroRedirectCount() {
        assertThat(expiringUrl().getRedirectCount()).isZero();
    }
}
