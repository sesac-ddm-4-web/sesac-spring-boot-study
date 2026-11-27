package com.example.studyservice.study.dto.res;

import com.example.studyservice.study.domain.Study;
import com.example.studyservice.study.domain.StudyMember;
import com.example.studyservice.study.domain.StudyRole;
import com.example.studyservice.study.domain.StudyStatus;

import java.time.LocalDateTime;

public record MyStudyRes(
        Long studyId,
        String title,
        StudyStatus status,
        int capacity,
        int memberCount,
        StudyRole myRole,
        LocalDateTime joinedAt
) {
    public static MyStudyRes of(StudyMember studyMember) {
        Study study = studyMember.getStudy();
        return new MyStudyRes(study.getId(), study.getTitle(), study.getStatus(), study.getCapacity(),
                study.getMemberCount(), studyMember.getRole(), studyMember.getJoinedAt());
    }
}
