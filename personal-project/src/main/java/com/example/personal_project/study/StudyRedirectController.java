package com.example.personal_project.study;

import com.example.personal_project.domain.ShortLinkRepository;
import com.example.personal_project.domain.StudySessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import java.net.URI;

@RestController
public class StudyRedirectController {
    private final Resolver resolver;
    public StudyRedirectController(Resolver resolver) { this.resolver=resolver; }
    @GetMapping("/study/{code}") public ResponseEntity<?> redirect(@PathVariable String code) {
        String destination=resolver.resolve(code);
        if(destination==null) return ResponseEntity.status(404).contentType(MediaType.TEXT_HTML).body("""
                <!doctype html><html lang="ko"><meta charset="utf-8"><title>링크를 찾을 수 없습니다</title>
                <main><h1>링크를 찾을 수 없습니다</h1><p>공유받은 주소를 확인해주세요.</p><a href="/">회차 목록으로</a></main></html>
                """);
        return ResponseEntity.status(302).location(URI.create(destination)).cacheControl(CacheControl.noStore()).build();
    }
    @Service
    public static class Resolver {
        private final ShortLinkRepository links;
        private final StudySessionRepository sessions;
        private final StudyStore store;
        public Resolver(ShortLinkRepository links,StudySessionRepository sessions,StudyStore store) { this.links=links;this.sessions=sessions;this.store=store; }
        @Transactional public String resolve(String code) {
            var link=links.findByCode(code);
            if(link==null) return null;
            String destination;
            if(link.sessionId()!=null) {
                if(sessions.findById(link.sessionId())==null) return null;
                destination="/session.html?id="+link.sessionId();
            } else {
                var material=store.material(link.materialId());
                if(material==null) return null;
                destination=StudyValidation.url(material.originalUrl());
            }
            links.incrementRedirectCount(code);
            return destination;
        }
    }
}
