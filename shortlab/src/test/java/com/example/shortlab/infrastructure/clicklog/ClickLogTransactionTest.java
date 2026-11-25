package com.example.shortlab.infrastructure.clicklog;

import com.example.shortlab.application.SimpleShortenUrlService;
import com.example.shortlab.domain.ClickLogRepository;
import com.example.shortlab.domain.ShortenUrl;
import com.example.shortlab.domain.ShortenUrlRepository;
import com.example.shortlab.presentation.dto.ShortenUrlCreateRequestDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 클릭 로그 저장의 REQUIRES_NEW 동작을 실제 DB로 검증한다.
 * 테스트 메서드에는 @Transactional을 걸지 않는다. (걸면 테스트 트랜잭션에 묶여 검증이 무의미해진다)
 */
@SpringBootTest
class ClickLogTransactionTest {

    @Autowired private ClickLogRepository clickLogRepository;
    @Autowired private ShortenUrlRepository shortenUrlRepository;
    @Autowired private SimpleShortenUrlService simpleShortenUrlService;
    @Autowired private TransactionTemplate transactionTemplate;
    @Autowired private JdbcTemplate jdbcTemplate;

    private String key;

    @AfterEach
    void cleanUp() {
        if (key != null) {
            jdbcTemplate.update("delete from click_log where shorten_url_key = ?", key);
            jdbcTemplate.update("delete from shorten_url where shorten_url_key = ?", key);
        }
    }

    private String createUrl() {
        key = simpleShortenUrlService
                .generateShortenUrl(new ShortenUrlCreateRequestDto("https://www.google.com"))
                .getShortenUrlKey();
        return key;
    }

    @Test
    @DisplayName("바깥 트랜잭션 안에서 클릭 로그 저장이 실패해도 바깥 트랜잭션은 커밋되고 카운트 증가가 유지된다.")
    void clickLogFailureDoesNotRollbackOuterTransaction() {
        String key = createUrl();

        // REQUIRES_NEW가 아니면 로그 저장 실패로 바깥 트랜잭션이 rollback-only가 되어
        // 커밋 시 UnexpectedRollbackException이 발생하고 카운트 증가도 롤백된다.
        transactionTemplate.executeWithoutResult(status -> {
            shortenUrlRepository.incrementRedirectCount(key);
            try {
                // click_log.shorten_url_key는 length 16 -> 초과 길이로 저장 실패를 유도
                clickLogRepository.save("x".repeat(40), LocalDateTime.now());
            } catch (RuntimeException ignored) {
            }
        });

        ShortenUrl found = shortenUrlRepository.findShortenUrlByShortenUrlKey(key);
        assertEquals(1L, found.getRedirectCount());
    }

    @Test
    @DisplayName("바깥 트랜잭션이 롤백되어도 이미 저장한 클릭 로그는 독립적으로 커밋되어 남는다.")
    void clickLogIsCommittedIndependentlyOfOuterRollback() {
        String key = createUrl();

        transactionTemplate.executeWithoutResult(status -> {
            clickLogRepository.save(key, LocalDateTime.now());
            status.setRollbackOnly();
        });

        int logs = clickLogRepository.findPage(key, null, null, null, 10).size();
        assertEquals(1, logs);
    }
}
