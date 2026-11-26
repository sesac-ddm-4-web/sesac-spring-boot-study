package com.example.shorturl.concurrency;

import com.example.shorturl.global.exception.ApiException;
import com.example.shorturl.global.exception.ErrorCode;
import com.example.shorturl.link.dto.req.ShortUrlCreateReq;
import com.example.shorturl.link.dto.res.ShortUrlRes;
import com.example.shorturl.link.service.ShortUrlService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ConcurrencyTest {

    private static final int THREADS = 16;
    private static final URI SERVICE = URI.create("http://localhost:8080");
    private static final String CHROME = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 "
            + "(KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36";

    @Autowired
    private ShortUrlService service;

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
        assertTrue(done.await(60, TimeUnit.SECONDS));
        pool.shutdown();
    }

    @Test
    @DisplayName("16개 스레드가 같은 링크를 동시에 1만 번 열어도 DB의 방문 수와 오늘 통계는 정확히 10000이다.")
    void concurrentVisits() throws InterruptedException {
        service.create(new ShortUrlCreateReq("https://github.com", "toss-github", null, null), SERVICE);
        AtomicInteger failed = new AtomicInteger();

        runConcurrently(10_000, () -> {
            try {
                service.redirect("toss-github", CHROME, false);
            } catch (RuntimeException e) {
                failed.incrementAndGet();
            }
        });

        assertEquals(0, failed.get());
        assertEquals(10_000, service.findByKey("toss-github").visitCount());
        assertEquals(10_000, service.stats("toss-github", 1).periodCount());
    }

    @Test
    @DisplayName("100개 요청이 동시에 같은 짧은 주소를 만들면 DB에는 1개만 저장되고 99개는 DUPLICATE_CUSTOM_KEY다.")
    void concurrentSameCustomKey() throws InterruptedException {
        AtomicInteger success = new AtomicInteger();
        AtomicInteger duplicate = new AtomicInteger();
        AtomicInteger other = new AtomicInteger();

        runConcurrently(100, () -> {
            try {
                service.create(new ShortUrlCreateReq("https://github.com/" + Thread.currentThread().getName(),
                        "portfolio", null, null), SERVICE);
                success.incrementAndGet();
            } catch (ApiException e) {
                if (e.getErrorCode() == ErrorCode.DUPLICATE_CUSTOM_KEY) {
                    duplicate.incrementAndGet();
                } else {
                    other.incrementAndGet();
                }
            } catch (RuntimeException e) {
                other.incrementAndGet();
            }
        });

        assertEquals(1, success.get());
        assertEquals(99, duplicate.get());
        assertEquals(0, other.get());
        assertEquals(1, service.search("portfolio", null, null).stream()
                .map(ShortUrlRes::key)
                .filter("portfolio"::equals)
                .count());
    }
}
