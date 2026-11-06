package com.example.sesac_spring_boot_study.controller;
import com.example.sesac_spring_boot_study.service.GreetingService;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
public class GreetingController {
    private final GreetingService service;

    public GreetingController(GreetingService service) {
        this.service = service;
    }
    @GetMapping("/greet")
    public String greet(
            @RequestParam(name = "name", defaultValue = "방문자") String name) {
        return service.greet(name);
    }
}