package com.example.personal_project.application.study.command;

import com.example.personal_project.domain.study.model.StudySession;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public record SessionCommand(int sessionNo, String title, OffsetDateTime startsAt, String notice,
        String location, String meetingUrl, String preparation, String code) {
    public StudySession toSession(Long id, Long creator) {
        if (startsAt == null) throw new IllegalArgumentException("회차 일시를 확인해주세요.");
        return new StudySession(id, sessionNo, title,
                startsAt.withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime(), notice,
                location, meetingUrl, preparation, creator, null, null);
    }
}
