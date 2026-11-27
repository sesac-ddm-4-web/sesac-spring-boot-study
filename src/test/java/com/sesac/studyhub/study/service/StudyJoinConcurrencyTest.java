package com.sesac.studyhub.study.service;

import com.sesac.studyhub.global.exception.BusinessException;
import com.sesac.studyhub.member.domain.Member;
import com.sesac.studyhub.member.repository.MemberRepository;
import com.sesac.studyhub.study.domain.Study;
import com.sesac.studyhub.study.domain.StudyMember;
import com.sesac.studyhub.study.repository.StudyMemberRepository;
import com.sesac.studyhub.study.repository.StudyRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class StudyJoinConcurrencyTest {

    @Autowired StudyMemberService studyMemberService;
    @Autowired MemberRepository memberRepository;
    @Autowired StudyRepository studyRepository;
    @Autowired StudyMemberRepository studyMemberRepository;

    @Test
    void 정원_3명_스터디에_10명이_동시에_참여해도_정원을_넘지_않는다() throws InterruptedException {
        String prefix = "concurrency-" + System.currentTimeMillis();
        Member leader = memberRepository.save(Member.create(prefix + "-leader@test.com", "pw", "leader"));
        Study study = studyRepository.save(Study.create("동시성 테스트", null, 3, leader));
        studyMemberRepository.save(StudyMember.leader(study, leader));

        List<Long> memberIds = IntStream.range(0, 10)
            .mapToObj(i -> memberRepository.save(
                Member.create(prefix + "-" + i + "@test.com", "pw", "m" + i)).getId())
            .toList();

        int threadCount = memberIds.size();
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger();

        for (Long memberId : memberIds) {
            executor.submit(() -> {
                try {
                    studyMemberService.join(study.getId(), memberId);
                    successCount.incrementAndGet();
                } catch (BusinessException ignored) {
                    // STUDY_FULL 예상
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executor.shutdown();

        assertThat(successCount.get()).isEqualTo(2);                               // 스터디장 1 + 2명
        assertThat(studyMemberRepository.countByStudyId(study.getId())).isEqualTo(3);
    }
}