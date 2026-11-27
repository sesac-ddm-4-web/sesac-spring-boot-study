package com.example.studyservice.study.dto.res;

import com.example.studyservice.study.domain.Study;
import com.example.studyservice.study.domain.StudyMember;
import com.example.studyservice.study.domain.StudyStatus;

import java.time.LocalDateTime;

public record StudySummaryRes(
        Long id,
        String title,
        int capacity,
        int memberCount,
        StudyStatus status,
        LeaderRes leader,
        LocalDateTime createdAt
) {
    public static StudySummaryRes of(Study study, StudyMember leader) {
        return new StudySummaryRes(study.getId(), study.getTitle(), study.getCapacity(), study.getMemberCount(),
                study.getStatus(), LeaderRes.of(leader), study.getCreatedAt());
    }
}
