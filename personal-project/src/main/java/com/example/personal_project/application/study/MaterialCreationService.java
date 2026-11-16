package com.example.personal_project.application.study;

import com.example.personal_project.domain.study.exception.StudyException;
import com.example.personal_project.domain.study.model.MemberRole;
import com.example.personal_project.domain.study.model.StudyValidation;
import com.example.personal_project.domain.study.repository.MaterialRepository;
import com.example.personal_project.domain.study.repository.MemberRepository;
import com.example.personal_project.domain.study.repository.PresenterAssignmentRepository;
import com.example.personal_project.domain.study.repository.StudySessionRepository;
import com.example.personal_project.domain.url.model.ShortLink;
import com.example.personal_project.domain.url.repository.ShortLinkRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MaterialCreationService {
    private final MaterialRepository materials;
    private final MemberRepository members;
    private final PresenterAssignmentRepository assignments;
    private final ShortLinkRepository links;
    private final StudySessionRepository sessions;
    public MaterialCreationService(MaterialRepository materials, MemberRepository members, PresenterAssignmentRepository assignments,
            ShortLinkRepository links, StudySessionRepository sessions) {
        this.materials = materials; this.members = members; this.assignments = assignments; this.links = links; this.sessions = sessions;
    }
    @Transactional
    public long create(long sid, long uid, String title, String url, String code) {
        StudyValidation.positive(sid); StudyValidation.positive(uid);
        title = StudyValidation.text(title, 150, "자료 제목"); url = StudyValidation.url(url);
        if (sessions.findById(sid) == null) throw StudyException.missing();
        var member = members.member(uid);
        if (member == null || member.role() != MemberRole.PRESENTER || !assignments.assigned(sid, uid)) throw StudyException.forbidden();
        long id = materials.insertMaterial(sid, uid, title, url);
        links.insert(new ShortLink(null, code, null, id, 0));
        return id;
    }
}
