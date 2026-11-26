package com.example.personal_project.application.study;

import com.example.personal_project.domain.study.model.StudySession;
import com.example.personal_project.domain.study.repository.StudySessionRepository;
import com.example.personal_project.domain.url.model.ShortLink;
import com.example.personal_project.domain.url.repository.ShortLinkRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudySessionCreationService {

    private final StudySessionRepository sessions;
    private final ShortLinkRepository links;

    public StudySessionCreationService(StudySessionRepository sessions, ShortLinkRepository links) {
        this.sessions = sessions;
        this.links = links;
    }

    @Transactional
    public Long create(StudySession session, String code) {
        if (session == null || code == null || code.isBlank()) {
            throw new IllegalArgumentException("회차와 단축 코드가 필요합니다.");
        }

        Long sessionId = sessions.insert(session);
        links.insert(new ShortLink(null, code, sessionId, null, 0));
        return sessionId;
    }
}
