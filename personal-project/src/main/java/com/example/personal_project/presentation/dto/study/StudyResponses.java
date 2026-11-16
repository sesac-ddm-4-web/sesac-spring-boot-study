package com.example.personal_project.presentation.dto.study;

import com.example.personal_project.application.study.result.StudyViews.Presenter;
import java.time.OffsetDateTime;
import java.util.List;

public final class StudyResponses {
    private StudyResponses() {}
    public record Items<T>(List<T> items) {}
    public record SessionSummary(Long id, int sessionNo, String title, OffsetDateTime startsAt, String shortUrl, long materialCount) {}
    public record Material(Long id, Long sessionId, String sessionTitle, Presenter author, String title, String originalUrl, String code, String shortUrl, long redirectCount) {}
    public record SessionDetail(Long id, int sessionNo, String title, OffsetDateTime startsAt, String notice, String location,
            String meetingUrl, String preparation, String code, String shortUrl, long redirectCount, List<Presenter> presenters, List<Material> materials) {}
}
