package com.example.study_member.dto.response;

import com.example.study_member.domain.Member;
import com.example.study_member.domain.MemberProfile;

public record MemberDetailResponse(
        Long memberId,
        String name,
        String description
) {
    public static MemberDetailResponse from(MemberProfile memberProfile) {
        Member member = memberProfile.getMember();
        return new MemberDetailResponse(
                member.getId(),
                member.getName(),
                memberProfile.getDescription()
        );
    }
}
