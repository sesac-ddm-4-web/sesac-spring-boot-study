package com.work.shortenurl.service;

import com.work.shortenurl.config.AppProperties;
import com.work.shortenurl.domain.ShortenUrl;
import com.work.shortenurl.dto.ShortenUrlCreateRequest;
import com.work.shortenurl.dto.ShortenUrlCreateResponse;
import com.work.shortenurl.dto.ShortenUrlInformation;
import com.work.shortenurl.dto.ShortenUrlListResponse;
import com.work.shortenurl.exception.InvalidUrlException;
import com.work.shortenurl.exception.LackOfShortenUrlKeyException;
import com.work.shortenurl.exception.NotFoundShortenUrlException;
import com.work.shortenurl.fake.FakeShortenUrlRepository;
import com.work.shortenurl.fake.FixedKeyGenerator;
import com.work.shortenurl.keygen.KeyGenerator;
import com.work.shortenurl.keygen.RandomBase62KeyGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ShortenUrlServiceTest {

    private static final String BASE_URL = "http://localhost:8080";

    private FakeShortenUrlRepository shortenUrlRepository;

    @BeforeEach
    void setUp() {
        shortenUrlRepository = new FakeShortenUrlRepository();
    }

    private ShortenUrlService createService(KeyGenerator keyGenerator) {
        AppProperties appProperties = new AppProperties(BASE_URL);
        return new ShortenUrlService(
                shortenUrlRepository, keyGenerator, appProperties, new UrlValidator(appProperties));
    }

    @Test
    void 단축URL을_생성하면_키와_전체_주소를_반환한다() {
        ShortenUrlService service = createService(new FixedKeyGenerator("aBcD1234"));

        ShortenUrlCreateResponse response = service.createShortenUrl(
                new ShortenUrlCreateRequest("https://shop.com/1", "instagram"));

        assertThat(response.shortenUrlKey()).isEqualTo("aBcD1234");
        assertThat(response.shortenUrl()).isEqualTo("http://localhost:8080/aBcD1234");
        assertThat(response.channel()).isEqualTo("instagram");
    }

    @Test
    void 같은_원본URL로_두번_생성하면_서로_다른_키가_발급되고_둘_다_조회된다() {
        ShortenUrlService service = createService(new RandomBase62KeyGenerator());
        ShortenUrlCreateRequest request = new ShortenUrlCreateRequest("https://shop.com/1", "instagram");

        String firstKey = service.createShortenUrl(request).shortenUrlKey();
        String secondKey = service.createShortenUrl(request).shortenUrlKey();

        assertThat(firstKey).isNotEqualTo(secondKey);
        assertThat(service.getShortenUrlInformation(firstKey).originalUrl()).isEqualTo("https://shop.com/1");
        assertThat(service.getShortenUrlInformation(secondKey).originalUrl()).isEqualTo("https://shop.com/1");
    }

    @Test
    void 키가_중복되면_다시_생성한_키를_사용한다() {
        shortenUrlRepository.save(new ShortenUrl("https://other.com", "dupkey01", "tiktok"));
        ShortenUrlService service = createService(new FixedKeyGenerator("dupkey01", "newkey01"));

        ShortenUrlCreateResponse response = service.createShortenUrl(
                new ShortenUrlCreateRequest("https://shop.com/1", "instagram"));

        assertThat(response.shortenUrlKey()).isEqualTo("newkey01");
    }

    @Test
    void 키가_5번_연속_중복되면_예외가_발생한다() {
        shortenUrlRepository.save(new ShortenUrl("https://other.com", "dupkey01", "tiktok"));
        ShortenUrlService service = createService(new FixedKeyGenerator(
                "dupkey01", "dupkey01", "dupkey01", "dupkey01", "dupkey01"));

        assertThatThrownBy(() -> service.createShortenUrl(
                new ShortenUrlCreateRequest("https://shop.com/1", "instagram")))
                .isInstanceOf(LackOfShortenUrlKeyException.class);
    }

    @Test
    void 원본URL_앞뒤_공백은_제거하고_저장한다() {
        ShortenUrlService service = createService(new FixedKeyGenerator("aBcD1234"));

        ShortenUrlCreateResponse response = service.createShortenUrl(
                new ShortenUrlCreateRequest("  https://shop.com/1  ", "instagram"));

        assertThat(response.originalUrl()).isEqualTo("https://shop.com/1");
    }

    @Test
    void 단축URL_정보를_조회한다() {
        ShortenUrlService service = createService(new FixedKeyGenerator("aBcD1234"));
        service.createShortenUrl(new ShortenUrlCreateRequest("https://shop.com/1", "Instagram"));

        ShortenUrlInformation info = service.getShortenUrlInformation("aBcD1234");

        assertThat(info.originalUrl()).isEqualTo("https://shop.com/1");
        assertThat(info.channel()).isEqualTo("instagram");
        assertThat(info.redirectCount()).isZero();
        assertThat(info.lastRedirectedAt()).isNull();
    }

    @Test
    void 없는_키를_조회하면_예외가_발생한다() {
        ShortenUrlService service = createService(new RandomBase62KeyGenerator());

        assertThatThrownBy(() -> service.getShortenUrlInformation("zzzz9999"))
                .isInstanceOf(NotFoundShortenUrlException.class);
    }

    @Test
    void 원본URL로_필터링하면_해당_URL의_링크만_조회한다() {
        ShortenUrlService service = createService(new FixedKeyGenerator("insta001", "tiktok01", "other001"));
        service.createShortenUrl(new ShortenUrlCreateRequest("https://shop.com/1", "instagram"));
        service.createShortenUrl(new ShortenUrlCreateRequest("https://shop.com/1", "tiktok"));
        service.createShortenUrl(new ShortenUrlCreateRequest("https://shop.com/2", "instagram"));

        ShortenUrlListResponse result = service.getShortenUrls("https://shop.com/1");

        assertThat(result.items()).extracting(ShortenUrlInformation::channel)
                .containsExactlyInAnyOrder("instagram", "tiktok");
        assertThat(result.totalRedirectCount()).isZero();
    }

    @Test
    void 원본URL이_없으면_전체를_조회한다() {
        ShortenUrlService service = createService(new FixedKeyGenerator("insta001", "other001"));
        service.createShortenUrl(new ShortenUrlCreateRequest("https://shop.com/1", "instagram"));
        service.createShortenUrl(new ShortenUrlCreateRequest("https://shop.com/2", "instagram"));

        assertThat(service.getShortenUrls(null).items()).hasSize(2);
        assertThat(service.getShortenUrls("  ").items()).hasSize(2);
    }

    @Test
    void 잘못된_URL이면_키를_생성하지_않고_예외가_발생한다() {
        ShortenUrlService service = createService(new FixedKeyGenerator("aBcD1234"));

        assertThatThrownBy(() -> service.createShortenUrl(
                new ShortenUrlCreateRequest("javascript:alert(1)", "instagram")))
                .isInstanceOf(InvalidUrlException.class);
        assertThat(shortenUrlRepository.existsByShortenUrlKey("aBcD1234")).isFalse();
    }
}