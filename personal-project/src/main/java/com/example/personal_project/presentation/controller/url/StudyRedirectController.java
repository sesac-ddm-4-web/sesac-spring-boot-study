package com.example.personal_project.presentation.controller.url;

import com.example.personal_project.application.url.StudyLinkRedirectService;
import java.net.URI;
import org.springframework.context.annotation.Profile;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@Profile("!textbook")
public class StudyRedirectController {
    private final StudyLinkRedirectService resolver;
    public StudyRedirectController(StudyLinkRedirectService resolver) { this.resolver=resolver; }
    @GetMapping("/study/{code}") public ResponseEntity<?> redirect(@PathVariable String code) {
        String destination=resolver.resolve(code);
        if(destination==null) return ResponseEntity.status(404).contentType(MediaType.TEXT_HTML).body("""
                <!doctype html><html lang="ko"><meta charset="utf-8"><title>링크를 찾을 수 없습니다</title>
                <main><h1>링크를 찾을 수 없습니다</h1><p>공유받은 주소를 확인해주세요.</p><a href="/">회차 목록으로</a></main></html>
                """);
        return ResponseEntity.status(302).location(URI.create(destination)).cacheControl(CacheControl.noStore()).build();
    }
}
