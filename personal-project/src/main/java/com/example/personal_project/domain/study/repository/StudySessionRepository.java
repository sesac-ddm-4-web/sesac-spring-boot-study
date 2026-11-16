package com.example.personal_project.domain.study.repository;

import com.example.personal_project.domain.study.model.StudySession;

public interface StudySessionRepository {
    void updateSession(StudySession session);
    Long insert(StudySession session);
    StudySession findById(Long id);
}
