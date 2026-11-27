package com.example.study.service;

import java.util.List;

import com.example.study.common.exception.BusinessException;
import com.example.study.common.exception.ErrorCode;
import com.example.study.domain.Member;
import com.example.study.domain.Study;
import com.example.study.dto.CreateStudyRequest;
import com.example.study.dto.RecruitStatusRequest;
import com.example.study.dto.StudyDetailResponse;
import com.example.study.dto.StudySummaryResponse;
import com.example.study.repository.MemberRepository;
import com.example.study.repository.StudyRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 변경 작업은 행을 잠근 뒤 검사한다. READ_COMMITTED를 써서, 잠금 이후의 조회가
 * 트랜잭션 초반의 스냅샷이 아니라 최신 커밋 데이터를 읽게 한다.
 */
@Service
@Transactional(readOnly = true)
public class StudyService {

    private final StudyRepository studyRepository;
    private final MemberRepository memberRepository;

    public StudyService(StudyRepository studyRepository, MemberRepository memberRepository) {
        this.studyRepository = studyRepository;
        this.memberRepository = memberRepository;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public StudyDetailResponse open(Long memberId, CreateStudyRequest request) {
        // 같은 회원의 개설·참여가 동시에 겹침 검사를 통과하지 못하도록 회원을 먼저 잠근다.
        Member owner = memberRepository.findByIdForUpdate(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));
        Study study = Study.open(owner, request.name(), request.description(), request.capacity(),
                request.recruitStatus(), request.dayOfWeek(), request.startTime(), request.endTime());
        if (studyRepository.findAllByParticipant(memberId).stream().anyMatch(study::overlaps)) {
            throw new BusinessException(ErrorCode.TIME_OVERLAP);
        }
        studyRepository.save(study);
        return StudyDetailResponse.of(study, memberId);
    }

    public List<StudySummaryResponse> getStudies() {
        return studyRepository.findAllSummaries();
    }

    public StudyDetailResponse getStudy(Long memberId, Long studyId) {
        Study study = studyRepository.findWithOwnerById(studyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDY_NOT_FOUND));
        return StudyDetailResponse.of(study, memberId);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public StudyDetailResponse changeRecruitStatus(Long memberId, Long studyId, RecruitStatusRequest request) {
        // 참여 신청의 모집 상태 검사와 순서가 정해지도록 스터디를 잠근다.
        Study study = studyRepository.findByIdForUpdate(studyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDY_NOT_FOUND));
        study.changeRecruitStatus(memberId, request.recruitStatus());
        return StudyDetailResponse.of(study, memberId);
    }
}
