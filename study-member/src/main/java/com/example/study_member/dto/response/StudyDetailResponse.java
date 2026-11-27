package com.example.study_member.dto.response;

import com.example.study_member.domain.Study;

public record StudyDetailResponse(
        Long studyId,
        String title,
        String description,
        Integer capacity,
        Long creatorId
) {
    public static StudyDetailResponse from(Study study) {
        return new StudyDetailResponse(
                study.getId(),
                study.getTitle(),
                study.getDescription(),
                study.getCapacity(),
                study.getCreator().getId()
        );
    }
}
