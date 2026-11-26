package com.example.personal_project.application.study.result;

import com.example.personal_project.domain.study.model.StudySession;
import java.time.OffsetDateTime;
import java.util.List;

public final class StudyViews {
    private StudyViews() {}
    public record Presenter(Long id, String displayName) {}
    public record SessionSummary(Long id, int sessionNo, String title, OffsetDateTime startsAt, String code, long materialCount) {}
    public record MaterialView(Long id, Long sessionId, String sessionTitle, Presenter author, String title, String originalUrl, String code, long redirectCount) {}
    public record Link(String code, long redirectCount) {}
    public record SessionDetail(StudySession session, Link link, List<Presenter> presenters, List<MaterialView> materials) {}
}
