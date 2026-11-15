package com.work.shortenurl.repository;

import com.work.shortenurl.domain.ShortenUrl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(ShortenUrlRepositoryImpl.class)
class ShortenUrlRepositoryImplTest {

    @Autowired
    private ShortenUrlRepository shortenUrlRepository;   // 인터페이스 타입으로 주입

    @Test
    void 저장한_단축URL을_키로_조회한다() {
        shortenUrlRepository.save(new ShortenUrl("https://shop.com/1", "aBcD1234", "instagram"));

        Optional<ShortenUrl> found = shortenUrlRepository.findByShortenUrlKey("aBcD1234");

        assertThat(found).isPresent();
        assertThat(found.get().getOriginalUrl()).isEqualTo("https://shop.com/1");
        assertThat(found.get().getRedirectCount()).isZero();
    }

    @Test
    void 키_존재_여부를_확인한다() {
        shortenUrlRepository.save(new ShortenUrl("https://shop.com/1", "aBcD1234", "instagram"));

        assertThat(shortenUrlRepository.existsByShortenUrlKey("aBcD1234")).isTrue();
        assertThat(shortenUrlRepository.existsByShortenUrlKey("zzzz9999")).isFalse();
    }

    @Test
    void 같은_키로_저장하면_예외가_발생한다() {
        shortenUrlRepository.save(new ShortenUrl("https://shop.com/1", "aBcD1234", "instagram"));

        assertThatThrownBy(() ->
                shortenUrlRepository.save(new ShortenUrl("https://shop.com/2", "aBcD1234", "tiktok"))
        ).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void 리다이렉트_카운트를_증가시킨다() {
        shortenUrlRepository.save(new ShortenUrl("https://shop.com/1", "aBcD1234", "instagram"));

        shortenUrlRepository.increaseRedirectCount("aBcD1234", LocalDateTime.now());
        shortenUrlRepository.increaseRedirectCount("aBcD1234", LocalDateTime.now());

        ShortenUrl found = shortenUrlRepository.findByShortenUrlKey("aBcD1234").orElseThrow();
        assertThat(found.getRedirectCount()).isEqualTo(2);
        assertThat(found.getLastRedirectedAt()).isNotNull();
    }

    @Test
    void 없는_키의_카운트를_증가시키면_0을_반환한다() {
        int updated = shortenUrlRepository.increaseRedirectCount("zzzz9999", LocalDateTime.now());

        assertThat(updated).isZero();
    }

    @Test
    void 같은_원본URL의_링크를_클릭수_내림차순으로_조회한다() {
        shortenUrlRepository.save(new ShortenUrl("https://shop.com/1", "insta001", "instagram"));
        shortenUrlRepository.save(new ShortenUrl("https://shop.com/1", "tiktok01", "tiktok"));
        shortenUrlRepository.save(new ShortenUrl("https://shop.com/2", "other001", "instagram"));
        shortenUrlRepository.increaseRedirectCount("tiktok01", LocalDateTime.now());

        List<ShortenUrl> result =
                shortenUrlRepository.findAllByOriginalUrlOrderByRedirectCountDesc("https://shop.com/1");

        assertThat(result).extracting(ShortenUrl::getShortenUrlKey)
                .containsExactly("tiktok01", "insta001");
    }

    @Test
    void 채널명은_소문자로_저장된다() {
        ShortenUrl saved = shortenUrlRepository.save(
                new ShortenUrl("https://shop.com/1", "aBcD1234", "  Instagram "));

        assertThat(saved.getChannel()).isEqualTo("instagram");
    }
}