package com.sesac.studyhub.study.dto;

import com.sesac.studyhub.study.domain.Study;
import com.sesac.studyhub.study.domain.StudyMember;
import com.sesac.studyhub.study.domain.StudyRole;
import com.sesac.studyhub.study.domain.StudyStatus;

import java.time.LocalDateTime;

public record MyStudyResponse(
    Long studyId,
    String name,
    StudyStatus status,
    int capacity,
    long currentMemberCount,
    StudyRole myRole,
    LocalDateTime joinedAt
) {
    public static MyStudyResponse of(StudyMember studyMember, long currentMemberCount) {
        Study study = studyMember.getStudy();
        return new MyStudyResponse(
            study.getId(), study.getName(), study.getStatus(), study.getCapacity(),
            currentMemberCount, studyMember.getRole(), studyMember.getCreatedAt());
    }
}