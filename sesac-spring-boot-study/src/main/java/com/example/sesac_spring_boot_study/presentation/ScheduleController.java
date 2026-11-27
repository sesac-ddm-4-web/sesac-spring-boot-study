package com.example.sesac_spring_boot_study.presentation;

import com.example.sesac_spring_boot_study.application.ScheduleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/studies/{studyId}/schedules")
public class ScheduleController {
    private final ScheduleService scheduleService;

    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ScheduleDto.Response create(@PathVariable Long studyId,
                                       @Valid @RequestBody ScheduleDto.CreateRequest request) {
        return scheduleService.create(studyId, request.memberId(), request.title(), request.startsAt());
    }

    @GetMapping
    public List<ScheduleDto.Response> findByStudy(@PathVariable Long studyId) {
        return scheduleService.findByStudy(studyId);
    }
}
