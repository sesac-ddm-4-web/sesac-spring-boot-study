package com.example.sesac_spring_boot_study.controller;

import com.example.sesac_spring_boot_study.service.StudyService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/study")
public class StudyController {
    private final StudyService service;

    public StudyController(StudyService service){
        this.service  = service;
    }

    @PostMapping("/participants")
    public String apply(@RequestParam(required = false) String name) {
        return service.apply(name);
    }

    @GetMapping("/participants")
    public String list() {
        return service.list();
    }

    @PostMapping("/cancel")
    public String cancel(@RequestParam(required = false) String memberId) {
        return service.cancel(memberId);
    }

    @PostMapping("/capacity")
    public String changeCapacity(@RequestParam(required = false) String capacity) {
        return service.changeCapacity(capacity);
    }
}


