package com.example.study.dto;

import com.example.study.domain.Member;
import com.example.study.domain.Profile;
import com.example.study.dto.validation.MaxUtf8Bytes;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignupRequest(
        @NotBlank(message = "이메일을 입력해 주세요.")
        @Email(message = "이메일 형식이 아닙니다.")
        @Size(max = Member.EMAIL_MAX_LENGTH, message = "이메일은 100자 이하여야 합니다.")
        String email,

        @NotBlank(message = "비밀번호를 입력해 주세요.")
        @Size(min = 8, max = 64, message = "비밀번호는 8자 이상 64자 이하여야 합니다.")
        @MaxUtf8Bytes(value = 72, message = "비밀번호는 UTF-8 기준 72바이트 이하여야 합니다.")
        String password,

        @NotBlank(message = "이름을 입력해 주세요.")
        @Size(max = Member.NAME_MAX_LENGTH, message = "이름은 30자 이하여야 합니다.")
        String name,

        @Size(max = Profile.INTRODUCTION_MAX_LENGTH, message = "자기소개는 500자 이하여야 합니다.")
        String introduction) {

    /** 검증 전에 이메일을 정규화해야 앞뒤 공백이 형식 오류가 되지 않는다. */
    public SignupRequest {
        email = Member.normalizeEmail(email);
    }

    @Override
    public String toString() {
        // 로그에 비밀번호가 남지 않게 한다.
        return "SignupRequest[email=" + email + ", name=" + name + "]";
    }
}
