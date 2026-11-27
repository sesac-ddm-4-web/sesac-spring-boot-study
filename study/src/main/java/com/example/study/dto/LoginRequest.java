package com.example.study.dto;

import com.example.study.domain.Member;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "이메일을 입력해 주세요.")
        String email,

        @NotBlank(message = "비밀번호를 입력해 주세요.")
        String password) {

    public LoginRequest {
        email = Member.normalizeEmail(email);
    }

    @Override
    public String toString() {
        return "LoginRequest[email=" + email + "]";
    }
}
