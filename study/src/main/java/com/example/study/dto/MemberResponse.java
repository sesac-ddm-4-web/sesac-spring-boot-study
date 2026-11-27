package com.example.study.dto;

import com.example.study.domain.Member;

/** 비밀번호 필드를 두지 않는다. */
public record MemberResponse(Long id, String email, String name, String introduction) {

    /** 프로필이 지연 로딩이므로 트랜잭션 안에서 호출한다. */
    public static MemberResponse from(Member member) {
        return new MemberResponse(
                member.getId(),
                member.getEmail(),
                member.getName(),
                member.getProfile().getIntroduction());
    }
}
