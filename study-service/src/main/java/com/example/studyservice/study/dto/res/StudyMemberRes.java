package com.example.studyservice.study.dto.res;

import com.example.studyservice.study.domain.StudyMember;
import com.example.studyservice.study.domain.StudyRole;

import java.time.LocalDateTime;

public record StudyMemberRes(
        Long studyId,
        Long memberId,
        String name,
        StudyRole role,
        LocalDateTime joinedAt
) {
    public static StudyMemberRes of(StudyMember studyMember) {
        return new StudyMemberRes(studyMember.getStudy().getId(), studyMember.getMember().getId(),
                studyMember.getMember().getName(), studyMember.getRole(), studyMember.getJoinedAt());
    }
}
