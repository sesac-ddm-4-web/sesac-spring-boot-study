package com.sesac.studyhub.study.dto;

import com.sesac.studyhub.member.domain.Member;
import com.sesac.studyhub.study.domain.StudyMember;
import com.sesac.studyhub.study.domain.StudyRole;

import java.time.LocalDateTime;

public record StudyMemberResponse(
    Long memberId,
    String name,
    String imageUrl,
    StudyRole role,
    LocalDateTime joinedAt
) {
    public static StudyMemberResponse from(StudyMember studyMember) {
        Member member = studyMember.getMember();
        return new StudyMemberResponse(
            member.getId(),
            member.getName(),
            member.getProfile().getImageUrl(),
            studyMember.getRole(),
            studyMember.getCreatedAt()
        );
    }
}