package com.example.study.controller;

import java.util.List;

import com.example.study.common.auth.LoginMember;
import com.example.study.dto.CreateScheduleRequest;
import com.example.study.dto.ScheduleResponse;
import com.example.study.service.ScheduleService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/studies/{studyId}/schedules")
public class ScheduleController {

    private final ScheduleService scheduleService;

    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ScheduleResponse register(@LoginMember Long memberId, @PathVariable("studyId") Long studyId,
            @Valid @RequestBody CreateScheduleRequest request) {
        return scheduleService.register(memberId, studyId, request);
    }

    @GetMapping
    public List<ScheduleResponse> getSchedules(@LoginMember Long memberId,
            @PathVariable("studyId") Long studyId) {
        return scheduleService.getSchedules(memberId, studyId);
    }
}
