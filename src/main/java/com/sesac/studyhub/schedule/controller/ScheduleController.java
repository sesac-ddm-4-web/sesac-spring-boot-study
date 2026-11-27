package com.sesac.studyhub.schedule.controller;

import com.sesac.studyhub.schedule.dto.ScheduleCreateRequest;
import com.sesac.studyhub.schedule.dto.ScheduleResponse;
import com.sesac.studyhub.schedule.service.ScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/studies/{studyId}/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    @PostMapping
    public ResponseEntity<ScheduleResponse> createSchedule(@PathVariable Long studyId,
        @AuthenticationPrincipal Long memberId,
        @Valid @RequestBody ScheduleCreateRequest request) {
        ScheduleResponse response = scheduleService.createSchedule(studyId, memberId, request);
        return ResponseEntity
            .created(URI.create("/api/studies/" + studyId + "/schedules/" + response.id()))
            .body(response);
    }

    @GetMapping
    public ResponseEntity<List<ScheduleResponse>> getSchedules(@PathVariable Long studyId) {
        return ResponseEntity.ok(scheduleService.getSchedules(studyId));
    }
}