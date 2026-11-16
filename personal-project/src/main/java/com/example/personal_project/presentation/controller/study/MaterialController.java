package com.example.personal_project.presentation.controller.study;

import com.example.personal_project.application.study.MaterialRegistrationService;
import com.example.personal_project.application.study.MaterialService;
import com.example.personal_project.application.study.StudyQueryService;
import com.example.personal_project.infrastructure.security.StudyPrincipal;
import com.example.personal_project.presentation.dto.study.MaterialInput;
import com.example.personal_project.presentation.dto.study.MaterialUpdate;
import com.example.personal_project.presentation.dto.study.StudyResponseMapper;
import com.example.personal_project.presentation.dto.study.StudyResponses.*;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class MaterialController {
    private final MaterialRegistrationService registration;
    private final MaterialService service;
    private final StudyQueryService queries;
    private final StudyResponseMapper mapper;
    public MaterialController(MaterialRegistrationService registration, MaterialService service, StudyQueryService queries, StudyResponseMapper mapper) {
        this.registration = registration; this.service = service; this.queries = queries; this.mapper = mapper;
    }
    @GetMapping("/materials") public Items<Material> list(@RequestParam(defaultValue="") String q, @RequestParam(required=false) Long sessionId) {
        return new Items<>(queries.materials(q, sessionId, null).stream().map(mapper::material).toList());
    }
    @GetMapping("/materials/{id}") public Material detail(@PathVariable long id) { return mapper.material(queries.material(id)); }
    @PostMapping("/sessions/{sid}/materials") public ResponseEntity<Material> create(@PathVariable long sid, @Valid @RequestBody MaterialInput input,
            @AuthenticationPrincipal StudyPrincipal user) {
        long id = registration.register(sid, user.id(), input.title(), input.originalUrl(), input.code());
        return ResponseEntity.created(URI.create("/api/materials/" + id)).body(detail(id));
    }
    @PutMapping("/materials/{id}") public Material update(@PathVariable long id, @Valid @RequestBody MaterialUpdate input, @AuthenticationPrincipal StudyPrincipal user) {
        service.update(id, user.id(), input.title(), input.originalUrl()); return detail(id);
    }
}
