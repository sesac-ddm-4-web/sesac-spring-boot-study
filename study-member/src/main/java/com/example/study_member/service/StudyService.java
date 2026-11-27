package com.example.study_member.service;

import com.example.study_member.domain.Member;
import com.example.study_member.domain.Study;
import com.example.study_member.domain.StudyMembership;
import com.example.study_member.dto.request.CreateStudyRequest;
import com.example.study_member.dto.response.StudyDetailResponse;
import com.example.study_member.dto.response.StudySummaryResponse;
import com.example.study_member.exception.MemberNotFoundException;
import com.example.study_member.exception.StudyNotFoundException;
import com.example.study_member.repository.MemberRepository;
import com.example.study_member.repository.StudyMeetingRepository;
import com.example.study_member.repository.StudyMembershipRepository;
import com.example.study_member.repository.StudyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StudyService {
    private final StudyRepository studyRepository;
    private final MemberRepository memberRepository;
    private final StudyMembershipRepository studyMembershipRepository;
    private final StudyMeetingRepository studyMeetingRepository;

    public StudyService(StudyRepository studyRepository, MemberRepository memberRepository, StudyMembershipRepository studyMembershipRepository, StudyMeetingRepository studyMeetingRepository) {
        this.studyRepository = studyRepository;
        this.memberRepository = memberRepository;
        this.studyMembershipRepository = studyMembershipRepository;
        this.studyMeetingRepository = studyMeetingRepository;
    }

    @Transactional
    public StudyDetailResponse createStudy(CreateStudyRequest request) {
        Member creator = memberRepository.findById(request.creatorId())
                .orElseThrow(() -> new MemberNotFoundException(request.creatorId()));
        Study study = new Study(
                creator,
                request.title(),
                request.description(),
                request.capacity()
        );
        Study savedStudy = studyRepository.save(study);
        StudyMembership membership = new StudyMembership(savedStudy, creator);
        studyMembershipRepository.save(membership);
        return StudyDetailResponse.from(savedStudy);
    }

    @Transactional(readOnly = true)
    public List<StudySummaryResponse> getStudyList() {
        return studyRepository.findAll()
                .stream()
                .map(StudySummaryResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public StudyDetailResponse getStudyDetail(Long studyId) {
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new StudyNotFoundException(studyId));

        return StudyDetailResponse.from(study);
    }

    @Transactional
    public void deleteStudy(Long studyId) {
        studyRepository.deleteById(studyId);
    }

    @Transactional
    public List<StudySummaryResponse> getJoinedStudies(Long memberId) {
        return studyMembershipRepository.findAllByMemberId(memberId)
                .stream()
                .map(membership -> StudySummaryResponse.from(membership.getStudy()))
                .toList();
    }
}
