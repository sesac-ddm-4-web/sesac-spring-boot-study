package com.example.study.controller;

import com.example.study.common.auth.PublicApi;
import com.example.study.common.auth.SessionConst;
import com.example.study.dto.LoginRequest;
import com.example.study.dto.MemberResponse;
import com.example.study.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PublicApi
    @PostMapping("/login")
    public MemberResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        // 인증에 성공한 뒤에 세션을 바꾼다. 실패하면 기존 세션은 그대로다.
        MemberResponse member = authService.authenticate(request);

        // 세션 고정 공격 방지: 기존 세션을 버리고 새 ID의 세션을 만든다.
        HttpSession oldSession = httpRequest.getSession(false);
        if (oldSession != null) {
            oldSession.invalidate();
        }
        httpRequest.getSession(true).setAttribute(SessionConst.MEMBER_ID, member.id());
        return member;
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest httpRequest) {
        HttpSession session = httpRequest.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }
}
