package com.sesac.studyhub.study.service;

import com.sesac.studyhub.global.exception.BusinessException;
import com.sesac.studyhub.global.exception.ErrorCode;
import com.sesac.studyhub.member.domain.Member;
import com.sesac.studyhub.member.repository.MemberRepository;
import com.sesac.studyhub.study.domain.Study;
import com.sesac.studyhub.study.domain.StudyMember;
import com.sesac.studyhub.study.dto.MyStudyResponse;
import com.sesac.studyhub.study.dto.StudyJoinResponse;
import com.sesac.studyhub.study.dto.StudyMemberCount;
import com.sesac.studyhub.study.dto.StudyMemberResponse;
import com.sesac.studyhub.study.repository.StudyMemberRepository;
import com.sesac.studyhub.study.repository.StudyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudyMemberService {

    private final StudyRepository studyRepository;
    private final StudyMemberRepository studyMemberRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public StudyJoinResponse join(Long studyId, Long memberId) {
        // 1. 스터디 행에 쓰기 락 → 같은 스터디 참여 요청은 한 줄로 처리됨
        Study study = studyRepository.findByIdForUpdate(studyId)
            .orElseThrow(() -> new BusinessException(ErrorCode.STUDY_NOT_FOUND));
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        // 2. 서비스 규칙 검사 (순서: 마감 → 중복 → 정원)
        if (!study.isRecruiting()) {
            throw new BusinessException(ErrorCode.STUDY_CLOSED);
        }
        if (studyMemberRepository.existsByStudyIdAndMemberId(studyId, memberId)) {
            throw new BusinessException(ErrorCode.ALREADY_JOINED);
        }
        long currentCount = studyMemberRepository.countByStudyId(studyId);
        if (currentCount >= study.getCapacity()) {
            throw new BusinessException(ErrorCode.STUDY_FULL);
        }

        studyMemberRepository.save(StudyMember.member(study, member));
        return new StudyJoinResponse(studyId, memberId, currentCount + 1);
    }

    @Transactional
    public void leave(Long studyId, Long memberId) {
        if (!studyRepository.existsById(studyId)) {
            throw new BusinessException(ErrorCode.STUDY_NOT_FOUND);
        }
        StudyMember studyMember = studyMemberRepository.findByStudyIdAndMemberId(studyId, memberId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_STUDY_MEMBER));

        if (studyMember.isLeader()) {
            throw new BusinessException(ErrorCode.LEADER_CANNOT_LEAVE);
        }
        studyMemberRepository.delete(studyMember);   // hard delete → 재참여 가능
    }

    public List<StudyMemberResponse> getStudyMembers(Long studyId) {
        if (!studyRepository.existsById(studyId)) {
            throw new BusinessException(ErrorCode.STUDY_NOT_FOUND);
        }
        return studyMemberRepository.findAllWithMemberByStudyId(studyId).stream()
            .map(StudyMemberResponse::from)
            .toList();
    }

    public List<MyStudyResponse> getMyStudies(Long memberId) {
        List<StudyMember> myStudyMembers = studyMemberRepository.findAllWithStudyByMemberId(memberId);
        if (myStudyMembers.isEmpty()) {
            return List.of();
        }

        List<Long> studyIds = myStudyMembers.stream().map(sm -> sm.getStudy().getId()).toList();
        Map<Long, Long> countMap = studyMemberRepository.countByStudyIds(studyIds).stream()
            .collect(Collectors.toMap(StudyMemberCount::studyId, StudyMemberCount::count));

        return myStudyMembers.stream()
            .map(sm -> MyStudyResponse.of(sm, countMap.getOrDefault(sm.getStudy().getId(), 0L)))
            .toList();
    }
}
