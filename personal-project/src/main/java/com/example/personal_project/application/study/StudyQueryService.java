package com.example.personal_project.application.study;

import com.example.personal_project.application.study.port.StudyQueryPort;
import com.example.personal_project.application.study.result.StudyViews.*;
import com.example.personal_project.domain.study.exception.StudyException;
import com.example.personal_project.domain.study.model.StudyValidation;
import com.example.personal_project.domain.study.repository.StudySessionRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class StudyQueryService {
    private final StudyQueryPort queries;
    private final StudySessionRepository sessions;
    public StudyQueryService(StudyQueryPort queries, StudySessionRepository sessions) { this.queries = queries; this.sessions = sessions; }
    public List<SessionSummary> sessions(String query, Long memberId) { return queries.sessions(StudyValidation.query(query), memberId); }
    public SessionDetail session(long id) {
        StudyValidation.positive(id);
        var session = sessions.findById(id);
        if (session == null) throw StudyException.missing();
        return new SessionDetail(session, queries.sessionLink(id), queries.sessionPresenters(id), queries.materials("", id, null, null));
    }
    public List<Presenter> presenters() { return queries.presenters(); }
    public List<MaterialView> materials(String query, Long sessionId, Long authorId) {
        if (sessionId != null) StudyValidation.positive(sessionId);
        return queries.materials(StudyValidation.query(query), sessionId, authorId, null);
    }
    public MaterialView material(long id) {
        StudyValidation.positive(id);
        var results = queries.materials("", null, null, id);
        if (results.isEmpty()) throw StudyException.missing();
        return results.getFirst();
    }
}
