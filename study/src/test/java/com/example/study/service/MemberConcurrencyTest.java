package com.example.study.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.example.study.DatabaseCleaner;
import com.example.study.IntegrationTest;
import com.example.study.common.exception.BusinessException;
import com.example.study.common.exception.ErrorCode;
import com.example.study.dto.SignupRequest;
import com.example.study.repository.MemberRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@IntegrationTest
class MemberConcurrencyTest {

    @Autowired
    MemberService memberService;
    @Autowired
    MemberRepository memberRepository;

    @Autowired
    DatabaseCleaner databaseCleaner;

    @Autowired
    JdbcTemplate jdbcTemplate;
    @Autowired
    PlatformTransactionManager transactionManager;

    @BeforeEach
    void clean() {
        databaseCleaner.clean();
    }

    @RepeatedTest(3)
    @DisplayName("같은 이메일로 동시에 가입하면 한 건만 성공하고 나머지는 이메일 중복이다")
    void concurrentSignUp() throws Exception {
        int threads = 8;
        var request = new SignupRequest("same@example.com", "password123", "이름", "");
        var start = new CountDownLatch(1);
        List<Future<ErrorCode>> futures = new ArrayList<>();

        try (ExecutorService executor = Executors.newFixedThreadPool(threads)) {
            for (int i = 0; i < threads; i++) {
                futures.add(executor.submit(() -> {
                    start.await();
                    try {
                        memberService.signUp(request);
                        return null;
                    } catch (BusinessException e) {
                        return e.errorCode();
                    }
                }));
            }
            start.countDown();
        }

        // BusinessException 외의 예외가 나면 get()에서 테스트가 실패한다.
        List<ErrorCode> results = new ArrayList<>();
        for (Future<ErrorCode> future : futures) {
            results.add(future.get(10, TimeUnit.SECONDS));
        }
        assertThat(results).filteredOn(code -> code == null).hasSize(1);
        assertThat(results).filteredOn(code -> code == ErrorCode.DUPLICATE_EMAIL).hasSize(threads - 1);
        assertThat(memberRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("먼저 넣던 가입이 취소돼 대기하던 가입들이 데드락에 빠져도, 한 건은 성공하고 나머지는 이메일 중복이다")
    void deadlockOnSameEmail() throws Exception {
        var request = new SignupRequest("same@example.com", "password123", "이름", "");
        List<Future<ErrorCode>> futures = new ArrayList<>();

        try (ExecutorService executor = Executors.newFixedThreadPool(3)) {
            new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
                jdbcTemplate.update("insert into profile (introduction) values ('')");
                jdbcTemplate.update("insert into member (email, password, name, profile_id) "
                        + "values ('same@example.com', 'x', 'x', last_insert_id())");
                for (int i = 0; i < 3; i++) {
                    futures.add(executor.submit(() -> {
                        try {
                            memberService.signUp(request);
                            return null;
                        } catch (BusinessException e) {
                            return e.errorCode();
                        }
                    }));
                }
                // 세 가입이 unique 인덱스의 잠금을 기다리게 한 뒤 롤백한다.
                assertThatThrownBy(() -> futures.getFirst().get(1, TimeUnit.SECONDS))
                        .isInstanceOf(TimeoutException.class);
                status.setRollbackOnly();
            });
        }

        List<ErrorCode> results = new ArrayList<>();
        for (Future<ErrorCode> future : futures) {
            results.add(future.get(10, TimeUnit.SECONDS));
        }
        assertThat(results).containsExactlyInAnyOrder(null, ErrorCode.DUPLICATE_EMAIL, ErrorCode.DUPLICATE_EMAIL);
        assertThat(memberRepository.count()).isEqualTo(1);
    }
}
