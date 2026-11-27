package com.example.studyservice.schedule.controller;

import com.example.studyservice.schedule.dto.req.ScheduleReq;
import com.example.studyservice.schedule.dto.res.ScheduleRes;
import com.example.studyservice.schedule.service.ScheduleService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/studies/{studyId}/schedules")
public class ScheduleController {

    private final ScheduleService scheduleService;

    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @PostMapping
    public ResponseEntity<ScheduleRes> createSchedule(@PathVariable Long studyId,
                                                      @AuthenticationPrincipal Long memberId,
                                                      @Valid @RequestBody ScheduleReq request) {
        ScheduleRes created = scheduleService.create(studyId, memberId, request);
        return ResponseEntity.created(URI.create("/studies/" + studyId + "/schedules/" + created.id()))
                .body(created);
    }

    @GetMapping
    public List<ScheduleRes> findSchedules(@PathVariable Long studyId, @AuthenticationPrincipal Long memberId) {
        return scheduleService.findAll(studyId, memberId);
    }

    @GetMapping("/{scheduleId}")
    public ScheduleRes findSchedule(@PathVariable Long studyId, @PathVariable Long scheduleId,
                                    @AuthenticationPrincipal Long memberId) {
        return scheduleService.findOne(studyId, scheduleId, memberId);
    }

    @PutMapping("/{scheduleId}")
    public ScheduleRes updateSchedule(@PathVariable Long studyId, @PathVariable Long scheduleId,
                                      @AuthenticationPrincipal Long memberId,
                                      @Valid @RequestBody ScheduleReq request) {
        return scheduleService.update(studyId, scheduleId, memberId, request);
    }

    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<Void> deleteSchedule(@PathVariable Long studyId, @PathVariable Long scheduleId,
                                               @AuthenticationPrincipal Long memberId) {
        scheduleService.delete(studyId, scheduleId, memberId);
        return ResponseEntity.noContent().build();
    }
}
