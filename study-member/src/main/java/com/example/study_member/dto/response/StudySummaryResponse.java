package com.example.study_member.dto.response;

import com.example.study_member.domain.Study;

public record StudySummaryResponse(
        Long studyId,
        String title,
        Integer capacity) {
    public static StudySummaryResponse from(Study study) {
        return new StudySummaryResponse(
                study.getId(),
                study.getTitle(),
                study.getCapacity()
        );
    }
}
