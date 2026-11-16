package com.example.personal_project.application.study;

import com.example.personal_project.domain.study.exception.StudyException;
import com.example.personal_project.domain.study.model.MemberRole;
import com.example.personal_project.domain.study.model.StudyValidation;
import com.example.personal_project.domain.study.repository.MemberRepository;
import com.example.personal_project.domain.study.repository.PresenterAssignmentRepository;
import com.example.personal_project.domain.study.repository.StudySessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PresenterAssignmentService {
    private final StudySessionRepository sessions;
    private final MemberRepository members;
    private final PresenterAssignmentRepository assignments;
    public PresenterAssignmentService(StudySessionRepository sessions, MemberRepository members, PresenterAssignmentRepository assignments) {
        this.sessions = sessions; this.members = members; this.assignments = assignments;
    }
    @Transactional
    public void assign(long sessionId, long memberId) {
        StudyValidation.positive(sessionId); StudyValidation.positive(memberId);
        if (sessions.findById(sessionId) == null) throw StudyException.missing();
        var member = members.member(memberId);
        if (member == null) throw StudyException.missing();
        if (member.role() != MemberRole.PRESENTER) throw StudyException.invalidPresenter();
        assignments.assign(sessionId, memberId);
    }
}
