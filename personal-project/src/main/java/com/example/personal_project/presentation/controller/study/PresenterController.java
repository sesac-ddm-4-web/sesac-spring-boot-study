package com.example.personal_project.presentation.controller.study;

import com.example.personal_project.application.study.PresenterAssignmentService;
import com.example.personal_project.application.study.StudyQueryService;
import com.example.personal_project.application.study.result.StudyViews.Presenter;
import com.example.personal_project.presentation.dto.study.StudyResponses.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class PresenterController {
    private final PresenterAssignmentService service;
    private final StudyQueryService queries;
    public PresenterController(PresenterAssignmentService service, StudyQueryService queries) { this.service = service; this.queries = queries; }
    @GetMapping("/presenters") public Items<Presenter> list() { return new Items<>(queries.presenters()); }
    @PutMapping("/sessions/{sid}/presenters/{uid}") public ResponseEntity<Void> assign(@PathVariable long sid, @PathVariable long uid) {
        service.assign(sid, uid); return ResponseEntity.noContent().build();
    }
}
