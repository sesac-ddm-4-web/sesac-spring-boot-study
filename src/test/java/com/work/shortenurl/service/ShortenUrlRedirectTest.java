package com.work.shortenurl.service;

import com.work.shortenurl.dto.ShortenUrlCreateRequest;
import com.work.shortenurl.dto.ShortenUrlInformation;
import com.work.shortenurl.exception.NotFoundShortenUrlException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
public class ShortenUrlRedirectTest {

    @Autowired
    private ShortenUrlService shortenUrlService;

    @Test
    void 리다이렉트하면_원본URL을_반환하고_카운트가_1_증가한다() {
        String key = shortenUrlService.createShortenUrl(
                new ShortenUrlCreateRequest("https://shop.com/1", "instagram")).shortenUrlKey();

        String originalUrl = shortenUrlService.getOriginalUrlForRedirect(key);

        assertThat(originalUrl).isEqualTo("https://shop.com/1");
        ShortenUrlInformation info = shortenUrlService.getShortenUrlInformation(key);
        assertThat(info.redirectCount()).isEqualTo(1);
        assertThat(info.lastRedirectedAt()).isNotNull();
    }

    @Test
    void 정보_조회는_카운트를_증가시키지_않는다() {
        String key = shortenUrlService.createShortenUrl(
                new ShortenUrlCreateRequest("https://shop.com/1", "instagram")).shortenUrlKey();

        shortenUrlService.getShortenUrlInformation(key);
        shortenUrlService.getShortenUrlInformation(key);

        assertThat(shortenUrlService.getShortenUrlInformation(key).redirectCount()).isZero();
    }

    @Test
    void 없는_키로_리다이렉트하면_예외가_발생한다() {
        assertThatThrownBy(() -> shortenUrlService.getOriginalUrlForRedirect("zzzz9999"))
                .isInstanceOf(NotFoundShortenUrlException.class);
    }
}
