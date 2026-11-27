package com.sesac.studyhub.member.dto;

import com.sesac.studyhub.member.domain.Member;

public record MemberProfileResponse(
    Long id,
    String name,
    String imageUrl,
    String description
) {
    public static MemberProfileResponse from(Member member) {
        return new MemberProfileResponse(
            member.getId(),
            member.getName(),
            member.getProfile().getImageUrl(),
            member.getProfile().getDescription()
        );
    }
}