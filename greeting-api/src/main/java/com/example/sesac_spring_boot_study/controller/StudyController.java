package com.example.sesac_spring_boot_study.controller;

import com.example.sesac_spring_boot_study.service.StudyService;
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
    public String apply(@RequestParam(name = "name", required = false) String name){
        return service.apply(name);
    }

    @GetMapping("/study/participants")
    public String getParticipants(){
        return service.getParticipants();
    }

    @PostMapping("/study/cancel")
    public String cancel(@RequestParam(name = "memberId", required = false) String memberId){
        return service.cancel(memberId);
    }

    @PostMapping("/study/capacity")
    public String changeCapacity(@RequestParam(name = "capacity", required = false) String capacity){
        return service.changeCapacity(capacity);
    }
}


