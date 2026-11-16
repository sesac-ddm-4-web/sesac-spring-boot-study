package com.example.personal_project.infrastructure.repository;

import com.example.personal_project.domain.ShortenUrl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class MapShortenUrlRepositoryTest {

    private static final String ORIGINAL_URL = "https://example.com/slides";
    private static final String KEY = "Ab23Cd45";

    private MapShortenUrlRepository repository;

    @BeforeEach
    void setUp() {
        repository = new MapShortenUrlRepository();
    }

    @Test
    void duplicateRegistrationPreservesOriginalUrlAndRedirectCount() {
        assertTrue(repository.saveShortenUrlIfAbsent(new ShortenUrl(ORIGINAL_URL, KEY)));
        repository.increaseRedirectCount(KEY);
        repository.increaseRedirectCount(KEY);

        boolean saved = repository.saveShortenUrlIfAbsent(
                new ShortenUrl("https://example.com/replacement", KEY));

        assertFalse(saved);
        ShortenUrl existing = repository.findShortenUrlByShortenUrlKey(KEY);
        assertEquals(ORIGINAL_URL, existing.getOriginalUrl());
        assertEquals(KEY, existing.getShortenUrlKey());
        assertEquals(2L, existing.getRedirectCount().longValue());
    }

    @Test
    void counterUpdatesPreserveOriginalUrlAndKey() {
        assertTrue(repository.saveShortenUrlIfAbsent(new ShortenUrl(ORIGINAL_URL, KEY)));

        ShortenUrl first = repository.increaseRedirectCount(KEY);
        assertNotNull(first);
        assertEquals(1L, first.getRedirectCount().longValue());

        ShortenUrl second = repository.increaseRedirectCount(KEY);
        assertNotNull(second);
        assertEquals(2L, second.getRedirectCount().longValue());
        assertEquals(ORIGINAL_URL, second.getOriginalUrl());
        assertEquals(KEY, second.getShortenUrlKey());
        assertEquals(2L, repository.findShortenUrlByShortenUrlKey(KEY)
                .getRedirectCount().longValue());
    }

    @Test
    void missingCounterUpdateDoesNotCreateLink() {
        assertNull(repository.increaseRedirectCount("missing"));
        assertNull(repository.findShortenUrlByShortenUrlKey("missing"));
    }

    @Test
    void concurrentRegistrationsForSameKeyHaveExactlyOneWinner() throws Exception {
        List<Callable<ShortenUrl>> tasks = new ArrayList<>();
        for (int attempt = 0; attempt < 8; attempt++) {
            ShortenUrl candidate = new ShortenUrl(
                    "https://example.com/slides/" + attempt, KEY);
            tasks.add(() -> repository.saveShortenUrlIfAbsent(candidate)
                    ? candidate : null);
        }

        List<ShortenUrl> winners = runConcurrently(tasks).stream()
                .filter(Objects::nonNull)
                .toList();

        assertEquals(1, winners.size());
        ShortenUrl saved = repository.findShortenUrlByShortenUrlKey(KEY);
        assertNotNull(saved);
        assertEquals(winners.getFirst().getOriginalUrl(), saved.getOriginalUrl());
        assertEquals(0L, saved.getRedirectCount().longValue());
    }

    @Test
    void concurrentCounterUpdatesDoNotLoseIncrements() throws Exception {
        assertTrue(repository.saveShortenUrlIfAbsent(new ShortenUrl(ORIGINAL_URL, KEY)));
        int workerCount = 8;
        int incrementsPerWorker = 1000;
        List<Callable<Boolean>> tasks = new ArrayList<>();
        for (int worker = 0; worker < workerCount; worker++) {
            tasks.add(() -> {
                for (int increment = 0; increment < incrementsPerWorker; increment++) {
                    assertNotNull(repository.increaseRedirectCount(KEY));
                }
                return true;
            });
        }

        runConcurrently(tasks);

        ShortenUrl saved = repository.findShortenUrlByShortenUrlKey(KEY);
        assertEquals((long) workerCount * incrementsPerWorker,
                saved.getRedirectCount().longValue());
        assertEquals(ORIGINAL_URL, saved.getOriginalUrl());
    }

    private <T> List<T> runConcurrently(List<Callable<T>> tasks) throws Exception {
        var executor = Executors.newFixedThreadPool(tasks.size());
        var ready = new CountDownLatch(tasks.size());
        var start = new CountDownLatch(1);
        try {
            List<Future<T>> futures = new ArrayList<>();
            for (Callable<T> task : tasks) {
                futures.add(executor.submit(() -> {
                    ready.countDown();
                    if (!start.await(10, TimeUnit.SECONDS)) {
                        throw new IllegalStateException("Concurrent test did not start in time");
                    }
                    return task.call();
                }));
            }
            assertTrue(ready.await(10, TimeUnit.SECONDS), "Workers did not become ready");
            start.countDown();

            List<T> results = new ArrayList<>();
            for (Future<T> future : futures) {
                results.add(future.get(10, TimeUnit.SECONDS));
            }
            return results;
        } finally {
            start.countDown();
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS),
                    "Concurrent test workers did not terminate");
        }
    }
}
