package com.example.studyservice.member.dto.res;

import com.example.studyservice.member.domain.Profile;

public record MemberProfileRes(
        Long memberId,
        String name,
        String nickname,
        String bio,
        String interests
) {
    public static MemberProfileRes of(Profile profile) {
        return new MemberProfileRes(profile.getMember().getId(), profile.getMember().getName(),
                profile.getNickname(), profile.getBio(), profile.getInterests());
    }
}
