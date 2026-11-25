package com.kdongd.shortari.repository;

import com.kdongd.shortari.domain.ShortUrl;
import com.kdongd.shortari.domain.UrlMode;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ShortUrlRepositoryTest {

    @Autowired
    private ShortUrlRepository repository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("저장하면 id와 생성 시각이 자동으로 채워진다")
    void saveTest() {
        ShortUrl saved = repository.save(
                new ShortUrl("https://www.naver.com", "aB3xK912", null, UrlMode.SHORT)
        );

        assertNotNull(saved.getId());
        assertNotNull(saved.getCreatedAt());
    }

    @Test
    @DisplayName("단축 코드로 조회할 수 있다")
    void findByShortCodeTest() {
        repository.save(
                new ShortUrl("https://www.naver.com", "aB3xK912", null, UrlMode.SHORT)
        );

        ShortUrl found = repository.findByShortCode("aB3xK912").orElseThrow();

        assertEquals("https://www.naver.com", found.getOriginalUrl());
        assertEquals(UrlMode.SHORT, found.getUrlMode());
    }

    @Test
    @DisplayName("존재하지 않는 단축 코드로 조회하면 빈 결과를 반환한다")
    void findByShortCodeNotFoundTest() {
        assertTrue(repository.findByShortCode("NOTFOUND").isEmpty());
    }

    @Test
    @DisplayName("표시 이름 존재 여부를 확인할 수 있다")
    void existsByDisplayNameTest() {
        repository.save(
                new ShortUrl("https://www.naver.com", "aB3xK912", "네이버", UrlMode.NAMING)
        );

        assertTrue(repository.existsByDisplayName("네이버"));
        assertFalse(repository.existsByDisplayName("구글"));
    }

    @Test
    @DisplayName("전체 목록은 최신순으로 조회된다")
    void findAllOrderTest() {
        ShortUrl first = repository.save(
                new ShortUrl("https://www.naver.com", "AAAAAAAA", null, UrlMode.SHORT)
        );
        ShortUrl second = repository.save(
                new ShortUrl("https://www.google.com", "BBBBBBBB", null, UrlMode.SHORT)
        );

        List<ShortUrl> result = repository.findAllByOrderByCreatedAtDescIdDesc();

        assertEquals(2, result.size());
        assertEquals(second.getId(), result.get(0).getId());
        assertEquals(first.getId(), result.get(1).getId());
    }

    @Test
    @DisplayName("같은 단축 코드를 저장하면 예외가 발생한다")
    void duplicateShortCodeTest() {
        repository.saveAndFlush(
                new ShortUrl("https://www.naver.com", "aB3xK912", null, UrlMode.SHORT)
        );

        assertThrows(
                DataIntegrityViolationException.class,
                () -> repository.saveAndFlush(
                        new ShortUrl("https://www.google.com", "aB3xK912", null, UrlMode.SHORT)
                )
        );
    }

    @Test
    @DisplayName("같은 표시 이름을 저장하면 예외가 발생한다")
    void duplicateDisplayNameTest() {
        repository.saveAndFlush(
                new ShortUrl("https://www.naver.com", "AAAAAAAA", "네이버", UrlMode.NAMING)
        );

        assertThrows(
                DataIntegrityViolationException.class,
                () -> repository.saveAndFlush(
                        new ShortUrl("https://www.google.com", "BBBBBBBB", "네이버", UrlMode.NAMING)
                )
        );
    }

    @Test
    @DisplayName("표시 이름이 없는 링크는 여러 개 저장할 수 있다")
    void multipleNullDisplayNameTest() {
        repository.saveAndFlush(
                new ShortUrl("https://www.naver.com", "AAAAAAAA", null, UrlMode.SHORT)
        );
        repository.saveAndFlush(
                new ShortUrl("https://www.google.com", "BBBBBBBB", null, UrlMode.SHORT)
        );

        assertEquals(2, repository.count());
    }

    @Test
    @DisplayName("엔티티의 표시 이름을 변경하면 변경 감지로 DB에 반영된다")
    void dirtyCheckingTest() {
        ShortUrl saved = repository.saveAndFlush(
                new ShortUrl("https://www.naver.com", "aB3xK912", "네이버", UrlMode.NAMING)
        );
        Long id = saved.getId();

        saved.changeDisplayName("네이버 메인");
        repository.flush();

        // 1차 캐시를 비워서 DB에서 다시 읽어오게 함
        entityManager.clear();

        ShortUrl found = repository.findById(id).orElseThrow();
        assertEquals("네이버 메인", found.getDisplayName());
    }
}