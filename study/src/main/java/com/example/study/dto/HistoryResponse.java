package com.example.study.dto;

import java.time.LocalDateTime;

import com.example.study.domain.HistoryReason;
import com.example.study.domain.StudyHistory;
import com.fasterxml.jackson.annotation.JsonFormat;

public record HistoryResponse(
        Long studyId,
        String studyName,
        HistoryReason reason,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "uuuu-MM-dd'T'HH:mm:ss") LocalDateTime endedAt) {

    public static HistoryResponse from(StudyHistory history) {
        return new HistoryResponse(history.getStudyId(), history.getStudyName(),
                history.getReason(), history.getEndedAt());
    }
}
