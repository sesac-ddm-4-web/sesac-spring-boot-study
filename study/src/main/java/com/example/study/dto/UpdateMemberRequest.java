package com.example.study.dto;

import com.example.study.domain.Member;
import com.example.study.domain.Profile;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** null인 필드는 수정하지 않는다. */
public record UpdateMemberRequest(
        // 보냈다면 공백만은 불가. (@NotBlank와 같은 기준이며 null은 통과)
        @Pattern(regexp = "(?s).*[^\\x00-\\x20].*", message = "이름은 공백만 입력할 수 없습니다.")
        @Size(max = Member.NAME_MAX_LENGTH, message = "이름은 30자 이하여야 합니다.")
        String name,

        @Size(max = Profile.INTRODUCTION_MAX_LENGTH, message = "자기소개는 500자 이하여야 합니다.")
        String introduction) {
}
