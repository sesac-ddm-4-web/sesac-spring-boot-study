package com.sesac.studyhub.study.dto;

import com.sesac.studyhub.member.domain.Member;

public record LeaderResponse(Long id, String name) {
    public static LeaderResponse from(Member leader) {
        return new LeaderResponse(leader.getId(), leader.getName());
    }
}
