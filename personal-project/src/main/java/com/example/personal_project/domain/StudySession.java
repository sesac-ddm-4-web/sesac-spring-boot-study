package com.example.personal_project.domain;

import java.time.LocalDateTime;

public record StudySession(
        Long id,
        int sessionNo,
        String title,
        LocalDateTime startsAt,
        String notice,
        String location,
        String meetingUrl,
        String preparation,
        Long createdBy,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
