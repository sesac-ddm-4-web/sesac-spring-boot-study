package com.sesac.studyhub.global.config;

import com.sesac.studyhub.member.domain.Member;
import com.sesac.studyhub.member.repository.MemberRepository;
import com.sesac.studyhub.schedule.domain.Schedule;
import com.sesac.studyhub.schedule.repository.ScheduleRepository;
import com.sesac.studyhub.study.domain.Study;
import com.sesac.studyhub.study.domain.StudyMember;
import com.sesac.studyhub.study.repository.StudyMemberRepository;
import com.sesac.studyhub.study.repository.StudyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class DataInitializer implements ApplicationRunner {

    private static final String PASSWORD = "password123";
    private static final String SEED_CHECK_EMAIL = "minjun@studyhub.com";

    private final MemberRepository memberRepository;
    private final StudyRepository studyRepository;
    private final StudyMemberRepository studyMemberRepository;
    private final ScheduleRepository scheduleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (memberRepository.existsByEmail(SEED_CHECK_EMAIL)) {
            log.info("[Seed] 샘플 데이터가 이미 있어서 건너뜁니다.");
            return;
        }

        // 회원 (비밀번호는 모두 password123)
        Member seoyeon = member("seoyeon@studyhub.com", "서연", "백엔드 개발자를 준비하고 있어요.");   // 시연용 계정: 아무 스터디에도 참여 안 함
        Member minjun  = member(SEED_CHECK_EMAIL,       "민준", "Spring과 JPA를 깊게 파고 있어요.");
        Member jiwoo   = member("jiwoo@studyhub.com",   "지우", "매일 알고리즘 한 문제씩 풀어요.");
        Member haeun   = member("haeun@studyhub.com",   "하은", "DB 성능 튜닝에 관심이 많아요.");
        Member doyun   = member("doyun@studyhub.com",   "도윤", "정보처리기사 실기 준비 중이에요.");

        // 1) 모집 중, 여유 있음 → 참여 시연용
        Study jpa = study("Spring JPA 스터디",
            "JPA 연관관계와 영속성 컨텍스트를 같이 공부해요.\n매주 목요일 저녁 7시, 강남에서 만나요.",
            5, minjun, jiwoo, haeun);
        schedule(jpa, "1주차 엔티티 매핑", "강남 스터디카페", 3);
        schedule(jpa, "2주차 연관관계 매핑", "강남 스터디카페", 10);
        schedule(jpa, "3주차 영속성 컨텍스트", "온라인 (Zoom)", 17);

        // 2) 모집 중이지만 정원 가득 → STUDY_FULL 시연용
        Study coding = study("코딩테스트 매일 1문제",
            "매일 한 문제씩 풀고 저녁에 풀이를 공유해요.",
            3, jiwoo, minjun, doyun);
        schedule(coding, "주간 풀이 리뷰", "온라인 (Discord)", 2);

        // 3) 모집 중, 여유 많음
        Study mysql = study("Real MySQL 읽기 모임",
            "Real MySQL 8.0을 한 챕터씩 읽고 정리해요.",
            6, haeun, doyun);
        schedule(mysql, "1장~2장 인덱스", "신촌 카페", 5);
        schedule(mysql, "3장 트랜잭션과 잠금", "신촌 카페", 12);

        // 4) 모집 마감 → STUDY_CLOSED 시연용
        Study cert = study("정처기 실기 대비",
            "기출 위주로 같이 풀어요. 모집은 마감했어요.",
            4, doyun, haeun);
        cert.close();
        schedule(cert, "기출 2023년 풀이", "온라인 (Zoom)", 4);

        log.info("[Seed] 샘플 데이터 생성 완료 - 로그인: seoyeon@studyhub.com / {}", PASSWORD);
    }

    private Member member(String email, String name, String description) {
        Member member = Member.create(email, passwordEncoder.encode(PASSWORD), name);
        member.updateProfile(null, description);
        return memberRepository.save(member);
    }

    private Study study(String name, String description, int capacity, Member leader, Member... members) {
        Study study = studyRepository.save(Study.create(name, description, capacity, leader));
        studyMemberRepository.save(StudyMember.leader(study, leader));
        List.of(members).forEach(m -> studyMemberRepository.save(StudyMember.member(study, m)));
        return study;
    }

    private void schedule(Study study, String title, String place, int daysFromToday) {
        LocalDateTime start = LocalDate.now().plusDays(daysFromToday).atTime(19, 0);
        scheduleRepository.save(Schedule.create(study, title, place, start, start.plusHours(2)));
    }
}