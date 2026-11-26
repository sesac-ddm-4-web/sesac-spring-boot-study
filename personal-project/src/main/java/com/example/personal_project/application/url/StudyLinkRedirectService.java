package com.example.personal_project.application.url;

import com.example.personal_project.application.url.port.StudyLinkTargetPort;
import com.example.personal_project.domain.url.repository.ShortLinkRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudyLinkRedirectService {
    private final ShortLinkRepository links;
    private final StudyLinkTargetPort targets;
    public StudyLinkRedirectService(ShortLinkRepository links, StudyLinkTargetPort targets) { this.links = links; this.targets = targets; }
    @Transactional
    public String resolve(String code) {
        var link = links.findByCode(code);
        if (link == null) return null;
        String destination = targets.destination(link);
        if (destination == null) return null;
        links.incrementRedirectCount(code);
        return destination;
    }
}
