package com.example.studyservice.auth.controller;

import com.example.studyservice.auth.dto.req.LoginReq;
import com.example.studyservice.auth.dto.req.ReissueReq;
import com.example.studyservice.auth.dto.req.SignupReq;
import com.example.studyservice.auth.dto.res.TokenRes;
import com.example.studyservice.auth.service.AuthService;
import com.example.studyservice.member.dto.res.MemberRes;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ResponseEntity<MemberRes> signup(@Valid @RequestBody SignupReq request) {
        MemberRes created = authService.signup(request);
        return ResponseEntity.created(URI.create("/members/" + created.id())).body(created);
    }

    @PostMapping("/login")
    public TokenRes login(@Valid @RequestBody LoginReq request) {
        return authService.login(request);
    }

    @PostMapping("/reissue")
    public TokenRes reissue(@Valid @RequestBody ReissueReq request) {
        return authService.reissue(request);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal Long memberId) {
        authService.logout(memberId);
        return ResponseEntity.noContent().build();
    }
}
