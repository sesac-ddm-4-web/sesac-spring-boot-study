package com.example.study.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.example.study.common.exception.BusinessException;
import com.example.study.common.exception.ErrorCode;

final class ConcurrencyTestSupport {

    /** 성공은 error가 {@code null}, 업무 오류는 ErrorCode */
    record Outcome(ErrorCode error) {
        boolean success() {
            return error == null;
        }
    }

    private ConcurrencyTestSupport() {
    }

    /** 작업을 동시에 시작한다. 데드락 등 업무 오류가 아닌 예외가 나면 테스트가 실패한다. */
    static List<Outcome> runTogether(List<Callable<?>> tasks) throws Exception {
        var start = new CountDownLatch(1);
        List<Future<Outcome>> futures = new ArrayList<>();
        try (ExecutorService executor = Executors.newFixedThreadPool(tasks.size())) {
            for (Callable<?> task : tasks) {
                futures.add(executor.submit(() -> {
                    start.await();
                    try {
                        task.call();
                        return new Outcome(null);
                    } catch (BusinessException e) {
                        return new Outcome(e.errorCode());
                    }
                }));
            }
            start.countDown();
        }
        List<Outcome> outcomes = new ArrayList<>();
        for (Future<Outcome> future : futures) {
            outcomes.add(future.get(30, TimeUnit.SECONDS));
        }
        return outcomes;
    }
}
