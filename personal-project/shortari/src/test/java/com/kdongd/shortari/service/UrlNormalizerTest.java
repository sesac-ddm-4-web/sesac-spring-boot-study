
package com.kdongd.shortari.service;

import com.kdongd.shortari.exception.InvalidUrlException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UrlNormalizerTest {

    private final UrlNormalizer normalizer = new UrlNormalizer();

    @Test
    @DisplayName("www로 시작하는 URL에는 https://를 자동으로 추가한다")
    void addHttpsTest() {
        String result = normalizer.normalize("www.naver.com");

        assertEquals("https://www.naver.com", result);
    }

    @Test
    @DisplayName("https://가 이미 존재하면 그대로 유지한다")
    void keepHttpsTest() {
        String result = normalizer.normalize("https://www.naver.com");

        assertEquals("https://www.naver.com", result);
    }

    @Test
    @DisplayName("http://가 이미 존재하면 그대로 유지한다")
    void keepHttpTest() {
        String result = normalizer.normalize("http://www.naver.com");

        assertEquals("http://www.naver.com", result);
    }

    @Test
    @DisplayName("URL 앞뒤에 공백이 있으면 제거한다")
    void trimUrlTest() {
        String result = normalizer.normalize("  www.naver.com  ");

        assertEquals("https://www.naver.com", result);
    }

    @Test
    @DisplayName("원본 URL이 null이면 예외가 발생한다")
    void nullUrlTest() {
        assertThrows(
                InvalidUrlException.class,
                () -> normalizer.normalize(null)
        );
    }

    @Test
    @DisplayName("원본 URL이 빈 문자열이면 예외가 발생한다")
    void emptyUrlTest() {
        assertThrows(
                InvalidUrlException.class,
                () -> normalizer.normalize("")
        );
    }

    @Test
    @DisplayName("원본 URL이 공백으로만 구성되면 예외가 발생한다")
    void blankUrlTest() {
        assertThrows(
                InvalidUrlException.class,
                () -> normalizer.normalize("   ")
        );
    }

    @Test
    @DisplayName("지원하지 않는 프로토콜을 입력하면 예외가 발생한다")
    void unsupportedProtocolTest() {
        assertThrows(
                InvalidUrlException.class,
                () -> normalizer.normalize("ftp://www.naver.com")
        );
    }

    @Test
    @DisplayName("도메인 형식이 올바르지 않으면 예외가 발생한다")
    void invalidDomainTest() {
        assertThrows(
                InvalidUrlException.class,
                () -> normalizer.normalize("잘못된 주소")
        );
    }

    @Test
    @DisplayName("localhost 주소는 등록할 수 없다")
    void localhostUrlTest() {
        assertThrows(
                InvalidUrlException.class,
                () -> normalizer.normalize("http://localhost:8080")
        );
    }

    @Test
    @DisplayName("URL에 사용자 정보가 포함되면 예외가 발생한다")
    void urlWithUserInfoTest() {
        assertThrows(
                InvalidUrlException.class,
                () -> normalizer.normalize(
                        "https://user:password@www.naver.com"
                )
        );
    }
}
