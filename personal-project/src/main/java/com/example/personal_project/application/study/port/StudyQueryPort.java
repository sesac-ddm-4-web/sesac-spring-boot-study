package com.example.personal_project.application.study.port;

import com.example.personal_project.application.study.result.StudyViews.*;
import java.util.List;

public interface StudyQueryPort {
    List<Presenter> presenters();
    List<Presenter> sessionPresenters(long id);
    List<SessionSummary> sessions(String query, Long memberId);
    List<MaterialView> materials(String query, Long sessionId, Long authorId, Long materialId);
    Link sessionLink(long id);
}
