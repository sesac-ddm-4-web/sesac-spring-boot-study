package com.example.studyservice.member.dto.res;

import com.example.studyservice.member.domain.Member;
import com.example.studyservice.member.domain.Profile;

import java.time.LocalDateTime;

public record MemberRes(
        Long id,
        String email,
        String name,
        LocalDateTime createdAt,
        ProfileRes profile
) {
    public static MemberRes of(Member member, Profile profile) {
        return new MemberRes(member.getId(), member.getEmail(), member.getName(), member.getCreatedAt(),
                ProfileRes.of(profile));
    }
}
