package com.sesac.studyhub.member.dto;

import com.sesac.studyhub.member.domain.Member;
import java.time.LocalDateTime;

public record MyInfoResponse(
    Long id,
    String email,
    String name,
    String imageUrl,
    String description,
    LocalDateTime createdAt
) {
    public static MyInfoResponse from(Member member) {
        return new MyInfoResponse(
            member.getId(),
            member.getEmail(),
            member.getName(),
            member.getProfile().getImageUrl(),
            member.getProfile().getDescription(),
            member.getCreatedAt()
        );
    }
}