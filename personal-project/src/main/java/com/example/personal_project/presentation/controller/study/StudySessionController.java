package com.example.personal_project.presentation.controller.study;

import com.example.personal_project.application.study.StudyQueryService;
import com.example.personal_project.application.study.StudySessionService;
import com.example.personal_project.infrastructure.security.StudyPrincipal;
import com.example.personal_project.presentation.dto.study.SessionInput;
import com.example.personal_project.presentation.dto.study.StudyResponseMapper;
import com.example.personal_project.presentation.dto.study.StudyResponses.*;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sessions")
public class StudySessionController {
    private final StudySessionService service;
    private final StudyQueryService queries;
    private final StudyResponseMapper mapper;
    public StudySessionController(StudySessionService service, StudyQueryService queries, StudyResponseMapper mapper) { this.service = service; this.queries = queries; this.mapper = mapper; }
    @GetMapping public Items<SessionSummary> list(@RequestParam(defaultValue="") String q) {
        return new Items<>(queries.sessions(q, null).stream().map(mapper::session).toList());
    }
    @GetMapping("/{id}") public SessionDetail detail(@PathVariable long id) { return mapper.detail(queries.session(id)); }
    @PostMapping public ResponseEntity<SessionDetail> create(@Valid @RequestBody SessionInput input, @AuthenticationPrincipal StudyPrincipal user) {
        long id = service.create(input.toCommand(), user.id());
        return ResponseEntity.created(URI.create("/api/sessions/" + id)).body(detail(id));
    }
    @PutMapping("/{id}") public SessionDetail update(@PathVariable long id, @Valid @RequestBody SessionInput input) {
        service.update(id, input.toCommand()); return detail(id);
    }
}
