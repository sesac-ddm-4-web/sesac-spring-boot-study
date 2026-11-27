package com.example.study_member.controller;

import com.example.study_member.dto.request.CreateStudyRequest;
import com.example.study_member.dto.response.StudyDetailResponse;
import com.example.study_member.dto.response.StudySummaryResponse;
import com.example.study_member.service.StudyService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/study")
public class StudyController {
    private final StudyService studyService;

    public StudyController(StudyService studyService) {
        this.studyService = studyService;
    }

    @PostMapping
    public ResponseEntity<StudyDetailResponse> createStudy(@Valid @RequestBody CreateStudyRequest request) {
        StudyDetailResponse response = studyService.createStudy(request);
        return ResponseEntity
                .created(URI.create("/study/%d".formatted(response.studyId())))
                .body(response);
    }

    @GetMapping("/list")
    public ResponseEntity<List<StudySummaryResponse>> getStudyList() {
        return ResponseEntity
                .ok(studyService.getStudyList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudyDetailResponse> getStudyDetail(@PathVariable Long id) {
        return ResponseEntity
                .ok(studyService.getStudyDetail(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudy(@PathVariable Long id) {
        studyService.deleteStudy(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<StudySummaryResponse>> getJoinedStudies(@RequestParam Long memberId) {
        return ResponseEntity
                .ok(studyService.getJoinedStudies(memberId));
    }
}
