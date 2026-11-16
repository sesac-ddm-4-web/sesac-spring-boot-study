package com.example.personal_project.study;

import com.example.personal_project.domain.ShortLink;
import com.example.personal_project.domain.ShortLinkRepository;
import com.example.personal_project.domain.StudySessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MaterialCreationService {
    private final StudyStore store;
    private final ShortLinkRepository links;
    private final StudySessionRepository sessions;
    public MaterialCreationService(StudyStore store, ShortLinkRepository links, StudySessionRepository sessions) {
        this.store=store; this.links=links; this.sessions=sessions;
    }
    @Transactional
    public long create(long sid, long uid, String title, String url, String code) {
        if (sessions.findById(sid)==null) throw StudyException.missing();
        var member=store.member(uid);
        if (member==null || !member.role().equals("PRESENTER") || !store.assigned(sid,uid)) throw StudyException.forbidden();
        long id=store.insertMaterial(sid,uid,title,url);
        links.insert(new ShortLink(null,code,null,id,0));
        return id;
    }
}
