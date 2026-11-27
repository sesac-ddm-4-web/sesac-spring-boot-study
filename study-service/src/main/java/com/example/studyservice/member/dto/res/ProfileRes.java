package com.example.studyservice.member.dto.res;

import com.example.studyservice.member.domain.Profile;

import java.time.LocalDateTime;

public record ProfileRes(
        String nickname,
        String bio,
        String interests,
        LocalDateTime updatedAt
) {
    public static ProfileRes of(Profile profile) {
        return new ProfileRes(profile.getNickname(), profile.getBio(), profile.getInterests(),
                profile.getUpdatedAt());
    }
}
