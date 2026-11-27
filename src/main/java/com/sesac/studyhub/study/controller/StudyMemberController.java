package com.sesac.studyhub.study.controller;

import com.sesac.studyhub.study.dto.MyStudyResponse;
import com.sesac.studyhub.study.dto.StudyJoinResponse;
import com.sesac.studyhub.study.dto.StudyMemberResponse;
import com.sesac.studyhub.study.service.StudyMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class StudyMemberController {

    private final StudyMemberService studyMemberService;

    @PostMapping("/api/studies/{studyId}/members")
    public ResponseEntity<StudyJoinResponse> join(@PathVariable Long studyId,
        @AuthenticationPrincipal Long memberId) {
        StudyJoinResponse response = studyMemberService.join(studyId, memberId);
        return ResponseEntity.created(URI.create("/api/studies/" + studyId + "/members"))
            .body(response);
    }

    @DeleteMapping("/api/studies/{studyId}/members/me")
    public ResponseEntity<Void> leave(@PathVariable Long studyId,
        @AuthenticationPrincipal Long memberId) {
        studyMemberService.leave(studyId, memberId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/studies/{studyId}/members")
    public ResponseEntity<List<StudyMemberResponse>> getStudyMembers(@PathVariable Long studyId) {
        return ResponseEntity.ok(studyMemberService.getStudyMembers(studyId));
    }

    @GetMapping("/api/members/me/studies")
    public ResponseEntity<List<MyStudyResponse>> getMyStudies(@AuthenticationPrincipal Long memberId) {
        return ResponseEntity.ok(studyMemberService.getMyStudies(memberId));
    }
}