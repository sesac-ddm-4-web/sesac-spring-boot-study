package com.example.study_member.dto.request;

import jakarta.validation.constraints.NotNull;

public record JoinStudyRequest(@NotNull Long memberId) {
}
