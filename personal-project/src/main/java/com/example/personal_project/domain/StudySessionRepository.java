package com.example.personal_project.domain;

public interface StudySessionRepository {
    Long insert(StudySession session);
    StudySession findById(Long id);
}
