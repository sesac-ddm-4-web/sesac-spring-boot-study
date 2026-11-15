package com.example.shorturl.link.domain;

import com.example.shorturl.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.net.URI;

import static com.example.shorturl.support.ErrorCodeAssertions.assertErrorCode;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class UrlPolicyTest {

    @ParameterizedTest
    @ValueSource(strings = {"https://github.com/daun", "http://example.com", "HTTPS://Example.com/a?b=1#c"})
    @DisplayName("http, https 절대 주소는 등록할 수 있다.")
    void allowHttpUrl(String url) {
        assertEquals(url, UrlPolicy.normalizeOriginalUrl(url));
    }

    @Test
    @DisplayName("앞뒤 공백은 제거된다.")
    void stripUrl() {
        assertEquals("https://example.com", UrlPolicy.normalizeOriginalUrl("  https://example.com  "));
    }

    @ParameterizedTest
    @CsvSource({
            "ftp://example.com, UNSUPPORTED_URL_SCHEME",
            "javascript:alert(1), UNSUPPORTED_URL_SCHEME",
            "mailto:a@b.com, UNSUPPORTED_URL_SCHEME",
            "example.com, UNSUPPORTED_URL_SCHEME",
            "/relative/path, UNSUPPORTED_URL_SCHEME",
            "https://, INVALID_URL_FORMAT",
            "https://exa mple.com, INVALID_URL_FORMAT",
            "http:///path, MISSING_URL_HOST",
            "'   ', ORIGINAL_URL_REQUIRED"
    })
    @DisplayName("http/https 절대 주소가 아니면 이유에 맞는 ErrorCode가 나온다.")
    void rejectInvalidUrl(String url, ErrorCode expected) {
        assertErrorCode(expected, () -> UrlPolicy.normalizeOriginalUrl(url));
    }

    @Test
    @DisplayName("2048자를 넘는 주소는 거절한다.")
    void rejectTooLongUrl() {
        String url = "https://example.com/" + "a".repeat(2048);
        assertErrorCode(ErrorCode.ORIGINAL_URL_TOO_LONG, () -> UrlPolicy.normalizeOriginalUrl(url));
    }

    @ParameterizedTest
    @ValueSource(strings = {"port", "portfolio", "my-resume_2026", "abcdefghijklmnopqrstuvwxyz0123"})
    @DisplayName("영문, 숫자, -, _ 4~30자는 원하는 짧은 주소로 쓸 수 있다.")
    void allowCustomKey(String key) {
        assertEquals(key, UrlPolicy.validateCustomKey(key));
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "포트폴리오", "my resume", "a.b.c.d", "a/bcd"})
    @DisplayName("형식에 맞지 않는 짧은 주소는 거절한다.")
    void rejectInvalidCustomKey(String key) {
        assertErrorCode(ErrorCode.INVALID_CUSTOM_KEY, () -> UrlPolicy.validateCustomKey(key));
    }

    @ParameterizedTest
    @ValueSource(strings = {"admin", "ADMIN", "index", "Static"})
    @DisplayName("예약어는 대소문자와 관계없이 짧은 주소로 쓸 수 없다.")
    void rejectReservedKey(String key) {
        assertErrorCode(ErrorCode.RESERVED_CUSTOM_KEY, () -> UrlPolicy.validateCustomKey(key));
    }

    @ParameterizedTest
    @ValueSource(strings = {"http://localhost:8080/abcd2345", "HTTP://LocalHost:8080", "http://localhost:8080/api/short-urls"})
    @DisplayName("서비스와 같은 호스트·포트를 가리키는 주소는 SELF_REFERENCING_URL이다.")
    void rejectSelfLink(String url) {
        URI service = URI.create("http://localhost:8080");
        assertErrorCode(ErrorCode.SELF_REFERENCING_URL, () -> UrlPolicy.validateNotSelfLink(url, service));
    }

    @Test
    @DisplayName("포트를 안 적으면 http 80, https 443으로 보고 비교한다.")
    void defaultPort() {
        assertErrorCode(ErrorCode.SELF_REFERENCING_URL,
                () -> UrlPolicy.validateNotSelfLink("https://portlink.kr/abcd", URI.create("https://portlink.kr:443")));
        assertDoesNotThrow(() -> UrlPolicy.validateNotSelfLink("http://localhost:3000/abcd", URI.create("http://localhost:8080")));
        assertDoesNotThrow(() -> UrlPolicy.validateNotSelfLink("https://github.com/daun", URI.create("http://localhost:8080")));
    }
}
