package com.sesac.studyhub.study.service;

import com.sesac.studyhub.global.exception.BusinessException;
import com.sesac.studyhub.global.exception.ErrorCode;
import com.sesac.studyhub.member.domain.Member;
import com.sesac.studyhub.member.repository.MemberRepository;
import com.sesac.studyhub.study.domain.Study;
import com.sesac.studyhub.study.domain.StudyMember;
import com.sesac.studyhub.study.repository.StudyMemberRepository;
import com.sesac.studyhub.study.repository.StudyRepository;
import com.sesac.studyhub.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 비관적 락(SELECT ... FOR UPDATE) 검증.
 * 락이 없으면 여러 요청이 동시에 "현재 인원 < 정원"을 통과해 정원을 넘는다.
 */
class StudyJoinConcurrencyTest extends IntegrationTestSupport {

    @Autowired StudyMemberService studyMemberService;
    @Autowired MemberRepository memberRepository;
    @Autowired StudyRepository studyRepository;
    @Autowired StudyMemberRepository studyMemberRepository;

    @Test
    @DisplayName("정원 3명 스터디에 10명이 동시에 참여해도 정원을 넘지 않는다")
    void concurrentJoin() throws InterruptedException {
        Member leader = memberRepository.save(Member.create("leader@test.com", "pw", "리더"));
        Study study = studyRepository.save(Study.create("동시성 테스트", null, 3, leader));
        studyMemberRepository.save(StudyMember.leader(study, leader));

        List<Long> applicantIds = IntStream.range(0, 10)
            .mapToObj(i -> memberRepository.save(Member.create("m" + i + "@test.com", "pw", "m" + i)).getId())
            .toList();

        int threadCount = applicantIds.size();
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger();
        Queue<ErrorCode> failures = new ConcurrentLinkedQueue<>();

        for (Long memberId : applicantIds) {
            executor.submit(() -> {
                ready.countDown();
                try {
                    start.await();                           // 모든 스레드가 동시에 출발
                    studyMemberService.join(study.getId(), memberId);
                    successCount.incrementAndGet();
                } catch (BusinessException e) {
                    failures.add(e.getErrorCode());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }
        ready.await();
        start.countDown();
        done.await();
        executor.shutdown();

        assertThat(successCount.get()).isEqualTo(2);         // 스터디장 1 + 2명 = 정원 3
        assertThat(failures).hasSize(8).containsOnly(ErrorCode.STUDY_FULL);
        assertThat(studyMemberRepository.countByStudyId(study.getId())).isEqualTo(3);
    }
}