package com.work.shortenurl.service;

import com.work.shortenurl.dto.ShortenUrlCreateRequest;
import com.work.shortenurl.repository.ShortenUrlJpaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
public class RedirectCountConcurrencyTest {

    @Autowired
    private ShortenUrlService shortenUrlService;

    @Autowired
    private ShortenUrlJpaRepository shortenUrlJpaRepository;

    @AfterEach
    void tearDown() {
        shortenUrlJpaRepository.deleteAll();
    }

    @Test
    void 동시에_100번_리다이렉트해도_카운트가_정확히_100이다() throws InterruptedException {
        String key = shortenUrlService.createShortenUrl(
                new ShortenUrlCreateRequest("https://shop.com/1", "instagram")).shortenUrlKey();

        int requestCount = 100;
        ExecutorService executor = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(requestCount);

        for (int i = 0; i < requestCount; i++) {
            executor.submit(() -> {
                try {
                    shortenUrlService.getOriginalUrlForRedirect(key);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executor.shutdown();

        long redirectCount = shortenUrlService.getShortenUrlInformation(key).redirectCount();
        assertThat(redirectCount).isEqualTo(requestCount);
    }
}
