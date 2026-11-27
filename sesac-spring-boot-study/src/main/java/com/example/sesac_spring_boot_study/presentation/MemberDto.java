package com.example.sesac_spring_boot_study.presentation;

import com.example.sesac_spring_boot_study.domain.Member;
import com.example.sesac_spring_boot_study.domain.Profile;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class MemberDto {
    private MemberDto() {}

    public record SignUpRequest(
            @NotBlank @Email String email,
            @NotBlank @Size(max = 30) String nickname
    ) {}

    public record ProfileUpdateRequest(
            @NotBlank @Size(max = 500) String introduction
    ) {}

    public record Response(Long id, String email, String nickname,
                           String introduction, LocalDateTime createdAt) {
        public static Response of(Member member, Profile profile) {
            return new Response(
                    member.getId(),
                    member.getEmail(),
                    member.getNickname(),
                    profile == null ? null : profile.getIntroduction(),
                    member.getCreatedAt()
            );
        }
    }
}
