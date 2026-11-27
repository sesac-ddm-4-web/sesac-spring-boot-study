package com.sesac.studyhub.study.service;

import com.sesac.studyhub.global.common.PageResponse;
import com.sesac.studyhub.global.exception.BusinessException;
import com.sesac.studyhub.global.exception.ErrorCode;
import com.sesac.studyhub.member.domain.Member;
import com.sesac.studyhub.member.repository.MemberRepository;
import com.sesac.studyhub.study.domain.Study;
import com.sesac.studyhub.study.domain.StudyMember;
import com.sesac.studyhub.study.domain.StudyStatus;
import com.sesac.studyhub.study.dto.*;
import com.sesac.studyhub.study.repository.StudyMemberRepository;
import com.sesac.studyhub.study.repository.StudyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudyService {

    private final StudyRepository studyRepository;
    private final StudyMemberRepository studyMemberRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public Long createStudy(Long memberId, StudyCreateRequest request) {
        Member leader = memberRepository.findById(memberId)
            .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        Study study = studyRepository.save(
            Study.create(request.name(), request.description(), request.capacity(), leader));
        studyMemberRepository.save(StudyMember.leader(study, leader));   // 개설자 = 스터디장으로 자동 참여

        return study.getId();
    }

    public PageResponse<StudySummaryResponse> getStudies(StudyStatus status, Pageable pageable) {
        Page<Study> studies = studyRepository.findAllWithLeader(status, pageable);

        List<Long> studyIds = studies.getContent().stream().map(Study::getId).toList();
        Map<Long, Long> countMap = studyIds.isEmpty() ? Map.of() :
            studyMemberRepository.countByStudyIds(studyIds).stream()
                .collect(Collectors.toMap(StudyMemberCount::studyId, StudyMemberCount::count));

        return PageResponse.from(studies.map(study ->
            StudySummaryResponse.of(study, countMap.getOrDefault(study.getId(), 0L))));
    }

    public StudyDetailResponse getStudy(Long studyId) {
        Study study = studyRepository.findWithLeaderById(studyId)
            .orElseThrow(() -> new BusinessException(ErrorCode.STUDY_NOT_FOUND));
        long count = studyMemberRepository.countByStudyId(studyId);
        return StudyDetailResponse.of(study, count);
    }

}
