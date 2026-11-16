package com.example.personal_project.presentation.controller.study;

import com.example.personal_project.application.study.StudyQueryService;
import com.example.personal_project.infrastructure.security.StudyPrincipal;
import com.example.personal_project.presentation.dto.study.StudyResponseMapper;
import com.example.personal_project.presentation.dto.study.StudyResponses.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/my")
public class MyStudyController {
    private final StudyQueryService queries;
    private final StudyResponseMapper mapper;
    public MyStudyController(StudyQueryService queries, StudyResponseMapper mapper) { this.queries = queries; this.mapper = mapper; }
    @GetMapping("/sessions") public Items<SessionSummary> sessions(@AuthenticationPrincipal StudyPrincipal user) {
        return new Items<>(queries.sessions("", user.id()).stream().map(mapper::session).toList());
    }
    @GetMapping("/materials") public Items<Material> materials(@AuthenticationPrincipal StudyPrincipal user) {
        return new Items<>(queries.materials("", null, user.id()).stream().map(mapper::material).toList());
    }
}
