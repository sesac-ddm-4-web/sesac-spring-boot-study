package com.example.studyservice.study;

import com.example.studyservice.global.exception.ApiException;
import com.example.studyservice.global.exception.ErrorCode;
import com.example.studyservice.study.dto.req.StudyCreateReq;
import com.example.studyservice.study.service.ParticipationService;
import com.example.studyservice.study.service.StudyService;
import com.example.studyservice.support.TestMembers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class JoinConcurrencyTest {

    private static final int CAPACITY = 5;
    private static final int APPLICANTS = 20;

    @Autowired
    private StudyService studyService;

    @Autowired
    private ParticipationService participationService;

    @Autowired
    private TestMembers testMembers;

    @Test
    @DisplayName("20명이 정원 5명 스터디에 동시에 참여해도 정확히 정원까지만 들어간다.")
    void concurrentJoinRespectsCapacity() throws InterruptedException {
        Long leaderId = testMembers.create("leader");
        Long studyId = studyService.create(leaderId, new StudyCreateReq("동시성 스터디", null, CAPACITY)).id();
        List<Long> applicants = new ArrayList<>();
        for (int i = 0; i < APPLICANTS; i++) {
            applicants.add(testMembers.create("applicant" + i));
        }

        AtomicInteger joined = new AtomicInteger();
        AtomicInteger full = new AtomicInteger();
        ExecutorService pool = Executors.newFixedThreadPool(APPLICANTS);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(APPLICANTS);
        for (Long applicantId : applicants) {
            pool.submit(() -> {
                try {
                    start.await();
                    participationService.join(studyId, applicantId);
                    joined.incrementAndGet();
                } catch (ApiException e) {
                    if (e.getErrorCode() == ErrorCode.STUDY_FULL) {
                        full.incrementAndGet();
                    }
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

        assertEquals(CAPACITY - 1, joined.get());
        assertEquals(APPLICANTS - (CAPACITY - 1), full.get());
        assertEquals(CAPACITY, studyService.findDetail(studyId).memberCount());
        assertEquals(CAPACITY, participationService.findMembers(studyId).size());
    }
}
