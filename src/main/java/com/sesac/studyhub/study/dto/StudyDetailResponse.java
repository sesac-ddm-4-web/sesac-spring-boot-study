package com.sesac.studyhub.study.dto;


import com.sesac.studyhub.study.domain.Study;
import com.sesac.studyhub.study.domain.StudyStatus;

import java.time.LocalDateTime;

public record StudyDetailResponse(
    Long id,
    String name,
    String description,
    int capacity,
    long currentMemberCount,
    StudyStatus status,
    LeaderResponse leader,
    LocalDateTime createdAt
) {
    public static StudyDetailResponse of(Study study, long currentMemberCount) {
        return new StudyDetailResponse(
            study.getId(), study.getName(), study.getDescription(), study.getCapacity(),
            currentMemberCount, study.getStatus(), LeaderResponse.from(study.getLeader()),
            study.getCreatedAt());
    }
}