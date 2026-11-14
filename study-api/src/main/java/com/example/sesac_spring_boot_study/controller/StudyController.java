package com.example.sesac_spring_boot_study.controller;

import com.example.sesac_spring_boot_study.service.StudyService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("study")
public class StudyController {

    private final StudyService studyService;

    public StudyController(StudyService studyService) {
        this.studyService = studyService;
    }

    @PostMapping("/participants")
    public String register(@RequestParam(required = false) String name) {
        return studyService.register(name);
    }

    @PostMapping("/cancel")
    public String cancel(@RequestParam(required = false) String memberId) {
        return studyService.cancel(memberId);
    }

    @PostMapping("/capacity")
    public String changeCapacity(@RequestParam(required = false) String capacity) {
        return studyService.changeCapacity(capacity);
    }

    @GetMapping("/participants")
    public String getMembers() {
        return studyService.getMembers();
    }
}
