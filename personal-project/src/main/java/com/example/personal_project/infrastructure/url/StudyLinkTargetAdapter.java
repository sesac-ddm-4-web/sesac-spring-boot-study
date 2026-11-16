package com.example.personal_project.infrastructure.url;

import com.example.personal_project.application.url.port.StudyLinkTargetPort;
import com.example.personal_project.domain.study.repository.MaterialRepository;
import com.example.personal_project.domain.study.repository.StudySessionRepository;
import com.example.personal_project.domain.url.model.OriginalUrl;
import com.example.personal_project.domain.url.model.ShortLink;
import org.springframework.stereotype.Component;

@Component
public class StudyLinkTargetAdapter implements StudyLinkTargetPort {
    private final StudySessionRepository sessions;
    private final MaterialRepository materials;
    public StudyLinkTargetAdapter(StudySessionRepository sessions, MaterialRepository materials) { this.sessions = sessions; this.materials = materials; }
    public String destination(ShortLink link) {
        if (link.sessionId() != null) return sessions.findById(link.sessionId()) == null ? null : "/session.html?id=" + link.sessionId();
        var material = materials.material(link.materialId());
        return material == null ? null : new OriginalUrl(material.originalUrl()).originalUrl();
    }
}
