package com.example.personal_project.domain.study.repository;



public interface PresenterAssignmentRepository {
    boolean assigned(long sessionId, long memberId);
    void assign(long sessionId, long memberId);
}
