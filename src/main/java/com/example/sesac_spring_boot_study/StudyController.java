package com.example.sesac_spring_boot_study;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StudyController {
    private final StudyService service;

    public StudyController(StudyService service){
        this.service = service;
    }

    @PostMapping("/study/participants")
    public String participate (
            @RequestParam(name = "name", required = false) String name) {
        return service.participate(name);
    }

    @GetMapping("/study/participants")
    public String getMemberList() {
        return service.getMemberList();
    }

}
