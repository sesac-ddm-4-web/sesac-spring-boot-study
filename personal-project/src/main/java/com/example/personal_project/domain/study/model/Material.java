package com.example.personal_project.domain.study.model;

import com.example.personal_project.domain.study.exception.StudyException;

public record Material(Long id, Long sessionId, Long authorId, String title, String originalUrl) {
    public void requireAuthor(long memberId) {
        if (!authorId.equals(memberId)) throw StudyException.forbidden();
    }
}
