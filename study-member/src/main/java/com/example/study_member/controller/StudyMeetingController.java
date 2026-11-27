package com.example.study_member.controller;

import com.example.study_member.dto.request.CreateStudyMeetingRequest;
import com.example.study_member.dto.response.StudyMeetingResponse;
import com.example.study_member.service.StudyMeetingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/meeting")
public class StudyMeetingController {
    private final StudyMeetingService studyMeetingService;

    public StudyMeetingController(StudyMeetingService studyMeetingService) {
        this.studyMeetingService = studyMeetingService;
    }

    @PostMapping
    public ResponseEntity<StudyMeetingResponse> createStudyMeeting(@RequestParam Long studyId, @Valid @RequestBody CreateStudyMeetingRequest request) {
        StudyMeetingResponse response = studyMeetingService.createStudyMeeting(studyId, request);
        return ResponseEntity
                .created(URI.create("/meeting/%d".formatted(response.meetingId())))
                .body(response);
    }

    @GetMapping("/list")
    public ResponseEntity<List<StudyMeetingResponse>> getStudyMeetingList(@RequestParam Long studyId) {
        return ResponseEntity
                .ok(studyMeetingService.getStudyMeetingList(studyId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudyMeeting(@PathVariable Long id) {
        studyMeetingService.deleteStudyMeeting(id);
        return ResponseEntity.noContent().build();
    }
}
