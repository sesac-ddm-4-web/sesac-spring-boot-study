package com.example.study_member.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CreateStudyRequest(
        @NotNull(message = "개설자 ID를 입력해 주세요.") Long creatorId,
        @NotNull(message = "스터디 제목을 입력해 주세요.") String title,
        String description,
        @NotNull(message = "스터디 정원을 입력해 주세요.")
        @Min(value = 1, message = "스터디 정원은 1명 이상이어야 합니다.") Integer capacity) {
}
