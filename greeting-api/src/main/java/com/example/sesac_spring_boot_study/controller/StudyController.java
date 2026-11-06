package com.example.sesac_spring_boot_study.controller;

import com.example.sesac_spring_boot_study.service.StudyService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/study")
public class StudyController {
    private final StudyService studyService;

    public StudyController(StudyService studyService) {
        this.studyService = studyService;
    }

    @PostMapping("/participants")
    public String registerMember(@RequestParam(name = "name", required = false) String name) {
        if (name == null || name.isBlank()) {
            return "이름이 비어 있습니다.";
        }
        return studyService.registerMember(name.strip());
    }

    @GetMapping("/participants")
    public String getMembers() {
        return studyService.getMembers();
    }

    @PostMapping("/cancel")
    public String cancelRegistration(@RequestParam(name = "memberId", required = false) String memberId) {
        try {
            int id = Integer.parseInt(memberId);
            return studyService.cancelRegistration(id);
        } catch (NumberFormatException e) {
            return "id는 숫자여야 합니다.";
        }
    }

    @PostMapping("/capacity")
    public String changeCapacity(@RequestParam(name = "capacity", required = false) String capacity) {
        try {
            int newCapacity = Integer.parseInt(capacity);
            return studyService.changeCapacity(newCapacity);
        } catch (NumberFormatException e) {
            return "capacity는 숫자여야 합니다.";
        }
    }
}
