package com.sesac.studyhub.study.controller;

import com.sesac.studyhub.global.common.PageResponse;
import com.sesac.studyhub.study.domain.StudyStatus;
import com.sesac.studyhub.study.dto.StudyCreateRequest;
import com.sesac.studyhub.study.dto.StudyCreateResponse;
import com.sesac.studyhub.study.dto.StudyDetailResponse;
import com.sesac.studyhub.study.dto.StudySummaryResponse;
import com.sesac.studyhub.study.service.StudyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/studies")
@RequiredArgsConstructor
public class StudyController {

    private final StudyService service;

    @PostMapping
    public ResponseEntity<StudyCreateResponse> createStudy(@AuthenticationPrincipal Long memberId,
        @Valid @RequestBody StudyCreateRequest request) {
        Long studyId = service.createStudy(memberId, request);
        return ResponseEntity.created(URI.create("/api/studies/" + studyId))
            .body(new StudyCreateResponse(studyId));
    }

    @GetMapping
    public ResponseEntity<PageResponse<StudySummaryResponse>> getStudies(
        @RequestParam(required = false) StudyStatus status,
        @ParameterObject
        @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(service.getStudies(status, pageable));
    }

    @GetMapping("/{studyId}")
    public ResponseEntity<StudyDetailResponse> getStudy(@PathVariable Long studyId) {
        return ResponseEntity.ok(service.getStudy(studyId));
    }
}
