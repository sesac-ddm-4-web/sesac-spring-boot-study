package com.example.personal_project.presentation.controller.study;

import com.example.personal_project.infrastructure.security.StudyPrincipal;
import java.util.Map;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class StudyAuthController {
    @GetMapping("/csrf") public Map<String,String> csrf(CsrfToken token) {
        return Map.of("headerName",token.getHeaderName(),"token",token.getToken());
    }
    @GetMapping("/me") public Map<String,Object> me(@AuthenticationPrincipal StudyPrincipal user) {
        return user.publicInfo();
    }
}
