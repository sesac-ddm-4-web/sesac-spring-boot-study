package com.example.studyservice.study.dto.res;

import com.example.studyservice.study.domain.Study;
import com.example.studyservice.study.domain.StudyMember;
import com.example.studyservice.study.domain.StudyStatus;

import java.time.LocalDateTime;

public record StudyDetailRes(
        Long id,
        String title,
        String description,
        int capacity,
        int memberCount,
        StudyStatus status,
        LeaderRes leader,
        LocalDateTime createdAt,
        LocalDateTime closedAt
) {
    public static StudyDetailRes of(Study study, StudyMember leader) {
        return new StudyDetailRes(study.getId(), study.getTitle(), study.getDescription(), study.getCapacity(),
                study.getMemberCount(), study.getStatus(), LeaderRes.of(leader), study.getCreatedAt(),
                study.getClosedAt());
    }
}
