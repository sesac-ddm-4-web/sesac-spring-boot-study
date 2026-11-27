package com.example.studyservice.support;

import com.example.studyservice.auth.dto.req.SignupReq;
import com.example.studyservice.auth.service.AuthService;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class TestMembers {

    private final AuthService authService;

    public TestMembers(AuthService authService) {
        this.authService = authService;
    }

    public Long create(String name) {
        String email = name + "-" + UUID.randomUUID() + "@test.com";
        return authService.signup(new SignupReq(email, "password1234", name, null)).id();
    }
}
