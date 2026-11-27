package com.example.study.dto;

import java.util.List;

public record MyStudiesResponse(
        List<StudySummaryResponse> ownedStudies,
        List<StudySummaryResponse> joinedStudies) {
}
