package com.example.studyservice.study.dto.req;

import jakarta.validation.constraints.NotNull;

public record LeaderDelegateReq(
        @NotNull
        Long memberId
) {
}
