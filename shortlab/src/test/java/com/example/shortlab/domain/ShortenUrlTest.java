package com.example.shortlab.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShortenUrlTest {

    @Test
    @DisplayName("여러 스레드가 동시에 접속 횟수를 증가시켜도 값이 유실되지 않아야 한다.")
    void incrementRedirectCountConcurrentlyTest() throws InterruptedException {
        // given
        ShortenUrl shortenUrl = new ShortenUrl("https://www.google.com", "aaaaaaaa");
        int threadCount = 32;
        int incrementsPerThread = 1_000;

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        // when: 모든 스레드를 준비시킨 뒤 동시에 출발
        for (int i = 0; i < threadCount; i++) {
            executor.execute(() -> {
                try {
                    startLatch.await();
                    for (int j = 0; j < incrementsPerThread; j++) {
                        shortenUrl.increaseRedirectCount();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
        }
        startLatch.countDown();
        assertTrue(doneLatch.await(10, TimeUnit.SECONDS));
        executor.shutdown();

        // then
        assertEquals((long) threadCount * incrementsPerThread, shortenUrl.getRedirectCount());
    }
}