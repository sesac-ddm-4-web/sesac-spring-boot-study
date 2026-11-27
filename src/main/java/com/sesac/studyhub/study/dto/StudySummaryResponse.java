package com.sesac.studyhub.study.dto;

import com.sesac.studyhub.study.domain.Study;
import com.sesac.studyhub.study.domain.StudyStatus;

import java.time.LocalDateTime;

public record StudySummaryResponse(
    Long id,
    String name,
    int capacity,
    long currentMemberCount,
    StudyStatus status,
    LeaderResponse leader,
    LocalDateTime createdAt
) {
    public static StudySummaryResponse of(Study study, long currentMemberCount) {
        return new StudySummaryResponse(
            study.getId(), study.getName(), study.getCapacity(), currentMemberCount,
            study.getStatus(), LeaderResponse.from(study.getLeader()), study.getCreatedAt());
    }
}