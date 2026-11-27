package com.example.study.dto;

import java.util.List;

public record MyPageResponse(
        Long id,
        String email,
        String name,
        String introduction,
        List<StudySummaryResponse> ownedStudies,
        List<StudySummaryResponse> joinedStudies,
        List<HistoryResponse> histories) {

    public static MyPageResponse of(MemberResponse member, MyStudiesResponse studies,
            List<HistoryResponse> histories) {
        return new MyPageResponse(member.id(), member.email(), member.name(), member.introduction(),
                studies.ownedStudies(), studies.joinedStudies(), histories);
    }
}
