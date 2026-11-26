package com.example.personal_project.domain.study.model;

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
    public StudySession {
        if (sessionNo < 1 || startsAt == null) {
            throw new IllegalArgumentException("회차 번호와 일시를 확인해주세요.");
        }
        title = StudyValidation.text(title, 150, "회차 제목");
        notice = StudyValidation.text(notice, 5000, "공지");
        location = StudyValidation.optional(location, 200);
        meetingUrl = StudyValidation.optional(meetingUrl, 2048);
        preparation = StudyValidation.optional(preparation, 2000);
        if (location == null && meetingUrl == null) {
            throw new IllegalArgumentException("장소 또는 온라인 접속 링크를 입력해주세요.");
        }
        if (meetingUrl != null) StudyValidation.url(meetingUrl);
    }
}
