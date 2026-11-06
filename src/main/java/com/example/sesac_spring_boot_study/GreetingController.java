package com.example.sesac_spring_boot_study;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
