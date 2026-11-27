package com.example.study.service;

import java.time.Clock;
import java.time.LocalDateTime;

import com.example.study.common.exception.BusinessException;
import com.example.study.common.exception.ErrorCode;
import com.example.study.domain.Member;
import com.example.study.domain.Study;
import com.example.study.dto.StudyDetailResponse;
import com.example.study.repository.MemberRepository;
import com.example.study.repository.StudyHistoryRepository;
import com.example.study.repository.StudyRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 참여와 탈퇴. 항상 회원 → 스터디 순서로 잠근 뒤 검사한다.
 * <ul>
 *   <li>회원 잠금: 같은 회원의 개설·참여를 한 줄로 세워 시간 겹침 검사를 지킨다.</li>
 *   <li>스터디 잠금: 같은 스터디의 참여·탈퇴·상태 변경·삭제를 한 줄로 세워 정원을 지킨다.</li>
 * </ul>
 */
@Service
@Transactional(isolation = Isolation.READ_COMMITTED)
public class StudyParticipationService {

    private final StudyRepository studyRepository;
    private final StudyHistoryRepository studyHistoryRepository;
    private final MemberRepository memberRepository;
    private final Clock clock;

    public StudyParticipationService(StudyRepository studyRepository,
            StudyHistoryRepository studyHistoryRepository, MemberRepository memberRepository, Clock clock) {
        this.studyRepository = studyRepository;
        this.studyHistoryRepository = studyHistoryRepository;
        this.memberRepository = memberRepository;
        this.clock = clock;
    }

    public StudyDetailResponse join(Long memberId, Long studyId) {
        Member member = lockMember(memberId);
        Study study = lockStudy(studyId);
        study.join(member, studyRepository.findAllByParticipant(memberId));
        return StudyDetailResponse.of(study, memberId);
    }

    public void leave(Long memberId, Long studyId) {
        lockMember(memberId);
        Study study = lockStudy(studyId);
        // 이력 저장과 참여 정보 삭제는 같은 트랜잭션이다.
        studyHistoryRepository.save(study.leave(memberId, LocalDateTime.now(clock)));
    }

    private Member lockMember(Long memberId) {
        return memberRepository.findByIdForUpdate(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));
    }

    private Study lockStudy(Long studyId) {
        return studyRepository.findByIdForUpdate(studyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDY_NOT_FOUND));
    }
}
