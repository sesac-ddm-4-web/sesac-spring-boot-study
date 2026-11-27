package com.example.study_member.controller;

import com.example.study_member.dto.request.JoinStudyRequest;
import com.example.study_member.dto.response.StudySummaryResponse;
import com.example.study_member.service.StudyMembershipService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/study/{studyId}/member")
public class StudyMembershipController {
    private final StudyMembershipService studyMembershipService;

    public StudyMembershipController(StudyMembershipService studyMembershipService) {
        this.studyMembershipService = studyMembershipService;
    }

    @PostMapping
    public ResponseEntity<StudySummaryResponse> joinStudy(@PathVariable Long studyId, @Valid @RequestBody JoinStudyRequest request) {
        return ResponseEntity.ok(studyMembershipService.joinStudy(studyId, request));
    }

    @DeleteMapping("/{memberId}")
    public ResponseEntity<StudySummaryResponse> leaveStudy(@PathVariable Long studyId, @PathVariable Long memberId) {
        return ResponseEntity.ok(studyMembershipService.leaveStudy(studyId, memberId));
    }
}
