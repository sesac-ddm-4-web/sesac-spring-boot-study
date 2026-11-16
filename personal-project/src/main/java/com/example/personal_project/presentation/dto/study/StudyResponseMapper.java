package com.example.personal_project.presentation.dto.study;

import com.example.personal_project.application.study.result.StudyViews;
import java.time.ZoneOffset;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class StudyResponseMapper {
    private final String base;
    public StudyResponseMapper(@Value("${app.base-url:http://localhost:8080}") String base) { this.base = base.replaceAll("/+$", ""); }
    private String shortUrl(String code) { return base + "/study/" + code; }
    public StudyResponses.SessionSummary session(StudyViews.SessionSummary s) {
        return new StudyResponses.SessionSummary(s.id(), s.sessionNo(), s.title(), s.startsAt(), shortUrl(s.code()), s.materialCount());
    }
    public StudyResponses.Material material(StudyViews.MaterialView m) {
        return new StudyResponses.Material(m.id(), m.sessionId(), m.sessionTitle(), m.author(), m.title(), m.originalUrl(), m.code(), shortUrl(m.code()), m.redirectCount());
    }
    public StudyResponses.SessionDetail detail(StudyViews.SessionDetail result) {
        var s = result.session(); var link = result.link();
        return new StudyResponses.SessionDetail(s.id(), s.sessionNo(), s.title(), s.startsAt().atOffset(ZoneOffset.UTC), s.notice(),
                s.location(), s.meetingUrl(), s.preparation(), link.code(), shortUrl(link.code()), link.redirectCount(),
                result.presenters(), result.materials().stream().map(this::material).toList());
    }
}
