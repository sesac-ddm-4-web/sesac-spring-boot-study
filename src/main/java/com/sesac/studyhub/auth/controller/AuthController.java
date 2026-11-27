package com.sesac.studyhub.auth.controller;

import com.sesac.studyhub.auth.dto.LoginRequest;
import com.sesac.studyhub.auth.dto.SignupRequest;
import com.sesac.studyhub.auth.dto.SignupResponse;
import com.sesac.studyhub.auth.dto.TokenResponse;
import com.sesac.studyhub.auth.service.AuthService;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService service;

    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup (@Valid @RequestBody SignupRequest request) {
        Long memberId = service.signup(request);

        return ResponseEntity.created(URI.create("/api/members/" + memberId))
            .body(new SignupResponse(memberId));
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login (@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(service.login(request));
    }
}
