package com.work.shortenurl.service;

import com.work.shortenurl.config.AppProperties;
import com.work.shortenurl.exception.InvalidUrlException;
import com.work.shortenurl.exception.SelfReferenceUrlException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class UrlValidatorTest {

    private final UrlValidator urlValidator =
            new UrlValidator(new AppProperties("http://localhost:8080"));

    @ParameterizedTest
    @ValueSource(strings = {
            "https://shop.com",
            "http://shop.com/product/123?ref=insta&id=1",
            "HTTPS://SHOP.COM",
            "https://shop.com/한글경로"
    })
    void 올바른_URL은_통과한다(String url) {
        assertThatCode(() -> urlValidator.validate(url)).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "안녕하세요",
            "naver.com",
            "javascript:alert(1)",
            "data:text/html,<script>alert(1)</script>",
            "ftp://shop.com/file",
            "file:///etc/passwd",
            "https://",
            "https://shop.com/a b"
    })
    void 잘못된_URL은_예외가_발생한다(String url) {
        assertThatThrownBy(() -> urlValidator.validate(url))
                .isInstanceOf(InvalidUrlException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "http://localhost:8080/aBcD1234",
            "http://LOCALHOST:8080/aBcD1234",
            "https://localhost/aBcD1234"
    })
    void 이_서비스의_URL은_단축할_수_없다(String url) {
        assertThatThrownBy(() -> urlValidator.validate(url))
                .isInstanceOf(SelfReferenceUrlException.class);
    }
}