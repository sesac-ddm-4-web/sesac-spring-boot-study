package com.example.studyservice.study.service;

import com.example.studyservice.global.exception.ApiException;
import com.example.studyservice.global.exception.ErrorCode;
import com.example.studyservice.study.domain.Study;
import com.example.studyservice.study.domain.StudyMember;
import com.example.studyservice.study.repository.StudyMemberRepository;
import com.example.studyservice.study.repository.StudyRepository;
import org.springframework.stereotype.Component;

@Component
public class StudyReader {

    private final StudyRepository studyRepository;
    private final StudyMemberRepository studyMemberRepository;

    public StudyReader(StudyRepository studyRepository, StudyMemberRepository studyMemberRepository) {
        this.studyRepository = studyRepository;
        this.studyMemberRepository = studyMemberRepository;
    }

    public Study findStudy(Long studyId) {
        return studyRepository.findById(studyId)
                .orElseThrow(() -> new ApiException(ErrorCode.STUDY_NOT_FOUND, studyId));
    }

    public Study findStudyForUpdate(Long studyId) {
        return studyRepository.findByIdForUpdate(studyId)
                .orElseThrow(() -> new ApiException(ErrorCode.STUDY_NOT_FOUND, studyId));
    }

    public StudyMember requireActiveMember(Long studyId, Long memberId) {
        return studyMemberRepository.findByStudyIdAndMemberId(studyId, memberId)
                .filter(StudyMember::isActive)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_STUDY_MEMBER));
    }

    public StudyMember requireLeader(Long studyId, Long memberId) {
        StudyMember studyMember = requireActiveMember(studyId, memberId);
        if (!studyMember.isLeader()) {
            throw new ApiException(ErrorCode.NOT_STUDY_LEADER);
        }
        return studyMember;
    }
}
