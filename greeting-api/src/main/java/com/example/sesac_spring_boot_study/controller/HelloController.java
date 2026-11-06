package com.example.sesac_spring_boot_study.controller;

import com.example.sesac_spring_boot_study.service.GreetingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
    private final GreetingService greetingService;

    public HelloController(GreetingService greetingService) {
        this.greetingService = greetingService;
    }

    @GetMapping("/hello")
    public String hello() {
        return "Hello, Spring!";
    }

    @GetMapping("/greet")
    public String greet(@RequestParam(name = "name", defaultValue = "방문자") String name){
        return greetingService.greet(name);
    }
}
