package com.example.shorturl.concurrency;

import com.example.shorturl.global.exception.ApiException;
import com.example.shorturl.global.exception.ErrorCode;
import com.example.shorturl.link.domain.RandomShortUrlKeyGenerator;
import com.example.shorturl.link.domain.ShortUrl;
import com.example.shorturl.link.dto.req.ShortUrlCreateReq;
import com.example.shorturl.link.repository.MemoryShortUrlRepository;
import com.example.shorturl.link.service.ShortUrlService;
import com.example.shorturl.support.MutableClock;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConcurrencyTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 14, 10, 0);
    private static final int THREADS = 16;

    // 모든 작업을 출발선에 세워 두고 한 번에 시작
    private void runConcurrently(int tasks, Runnable task) throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(THREADS);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(tasks);
        for (int i = 0; i < tasks; i++) {
            pool.submit(() -> {
                try {
                    start.await();
                    task.run();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }
        start.countDown();
        assertTrue(done.await(10, TimeUnit.SECONDS));
        pool.shutdown();
    }

    @Test
    @DisplayName("16개 스레드가 같은 링크를 동시에 1만 번 열어도 방문 수는 정확히 10000이다.")
    void concurrentVisits() throws InterruptedException {
        ShortUrl shortUrl = new ShortUrl("toss-github", "https://github.com", null, true, NOW, null);

        runConcurrently(10_000, () -> shortUrl.visit(NOW, false));

        assertEquals(10_000, shortUrl.getVisitCount());
        assertEquals(10_000, shortUrl.dailyVisits(NOW.toLocalDate(), NOW.toLocalDate()).get(0).count());
    }

    @Test
    @DisplayName("100개 요청이 동시에 같은 짧은 주소를 만들면 1개만 성공하고 99개는 DUPLICATE_CUSTOM_KEY다.")
    void concurrentSameCustomKey() throws InterruptedException {
        ShortUrlService service = new ShortUrlService(new MemoryShortUrlRepository(),
                new RandomShortUrlKeyGenerator(), new MutableClock(NOW));
        AtomicInteger success = new AtomicInteger();
        AtomicInteger duplicate = new AtomicInteger();

        runConcurrently(100, () -> {
            try {
                service.create(new ShortUrlCreateReq("https://github.com/" + Thread.currentThread().getName(),
                        "portfolio", null, null), URI.create("http://localhost:8080"));
                success.incrementAndGet();
            } catch (ApiException e) {
                if (e.getErrorCode() == ErrorCode.DUPLICATE_CUSTOM_KEY) {
                    duplicate.incrementAndGet();
                }
            }
        });

        assertEquals(1, success.get());
        assertEquals(99, duplicate.get());
        assertEquals(1, service.search(null, null, null).size());
    }
}
