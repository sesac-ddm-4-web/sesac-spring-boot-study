package com.example.study_member.service;

import com.example.study_member.domain.Member;
import com.example.study_member.domain.Study;
import com.example.study_member.domain.StudyMembership;
import com.example.study_member.dto.request.JoinStudyRequest;
import com.example.study_member.dto.response.StudySummaryResponse;
import com.example.study_member.exception.*;
import com.example.study_member.repository.MemberRepository;
import com.example.study_member.repository.StudyMembershipRepository;
import com.example.study_member.repository.StudyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudyMembershipService {
    private final MemberRepository memberRepository;
    private final StudyRepository studyRepository;
    private final StudyMembershipRepository studyMembershipRepository;

    public StudyMembershipService(StudyRepository studyRepository, StudyMembershipRepository studyMembershipRepository, MemberRepository memberRepository, StudyMembershipRepository studyMembershipRepository1) {
        this.studyRepository = studyRepository;
        this.memberRepository = memberRepository;
        this.studyMembershipRepository = studyMembershipRepository1;
    }

    @Transactional
    public StudySummaryResponse joinStudy(Long studyId, JoinStudyRequest request) {
        Study study = studyRepository.findLockedById(studyId)
                .orElseThrow(() -> new StudyNotFoundException(studyId));
        Member member = memberRepository.findById(request.memberId())
                .orElseThrow(() -> new MemberNotFoundException(request.memberId()));
        if (studyMembershipRepository.findByStudyIdAndMemberId(studyId, member.getId()).isPresent()) {
            throw new DuplicateStudyMembershipException();
        }
        if (study.isFull(studyMembershipRepository.countByStudyId(studyId))) {
            throw new StudyFullException(studyId, study.getCapacity());
        }
        StudyMembership membership = new StudyMembership(study, member);
        StudyMembership savedMembership = studyMembershipRepository.save(membership);
        return StudySummaryResponse.from(savedMembership.getStudy());
    }

    @Transactional
    public StudySummaryResponse leaveStudy(Long studyId, Long memberId) {
        StudyMembership membership = studyMembershipRepository
                .findByStudyIdAndMemberId(studyId, memberId)
                .orElseThrow(() -> new StudyMembershipNotFoundException(studyId, memberId));

        Study study = membership.getStudy();
        if (study.isCreator(memberId)) {
            throw new StudyCreatorCannotLeaveException();
        }

        studyMembershipRepository.delete(membership);
        return StudySummaryResponse.from(study);
    }
}
