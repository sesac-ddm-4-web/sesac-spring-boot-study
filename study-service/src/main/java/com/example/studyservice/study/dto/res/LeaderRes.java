package com.example.studyservice.study.dto.res;

import com.example.studyservice.study.domain.StudyMember;

public record LeaderRes(Long memberId, String name) {

    public static LeaderRes of(StudyMember leader) {
        return leader == null ? null : new LeaderRes(leader.getMember().getId(), leader.getMember().getName());
    }
}
