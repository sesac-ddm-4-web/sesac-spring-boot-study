package com.example.personal_project.application.study;

import com.example.personal_project.application.study.command.SessionCommand;
import com.example.personal_project.domain.study.exception.StudyException;
import com.example.personal_project.domain.study.model.StudyValidation;
import com.example.personal_project.domain.study.repository.StudySessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudySessionService {
    private final StudySessionRepository sessions;
    private final StudySessionRegistrationService registration;
    public StudySessionService(StudySessionRepository sessions, StudySessionRegistrationService registration) {
        this.sessions = sessions; this.registration = registration;
    }
    public long create(SessionCommand command, long userId) {
        return registration.register(command.toSession(null, userId), command.code()).sessionId();
    }
    @Transactional
    public void update(long id, SessionCommand command) {
        StudyValidation.positive(id);
        var existing = sessions.findById(id);
        if (existing == null) throw StudyException.missing();
        if (command.code() != null) throw new IllegalArgumentException("생성된 단축 코드는 변경할 수 없습니다.");
        sessions.updateSession(command.toSession(id, existing.createdBy()));
    }
}
