package com.example.sesac_spring_boot_study.service;

import org.springframework.stereotype.Service;

@Service
public class GreetingService {
    public String greet(String name){
        return "안녕하세요, " + name + "님";
    }
}
