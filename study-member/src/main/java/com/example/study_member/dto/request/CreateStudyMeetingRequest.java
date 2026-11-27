package com.example.study_member.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CreateStudyMeetingRequest(
        @NotNull(message = "회원 ID를 입력해 주세요.") Long memberId,
        @NotNull(message = "모임 제목을 입력해 주세요.") String title,
        @NotNull(message = "시작 시각을 입력해 주세요.") LocalDateTime startsAt,
        @NotNull(message = "종료 시각을 입력해 주세요.") LocalDateTime finishesAt,
        @NotNull(message = "모임 장소를 입력해 주세요.") String location,
        String description
) {
    @JsonIgnore
    @AssertTrue(message = "종료 시각은 시작 시각보다 늦어야 합니다.")
    public boolean isTimeRangeValid() {
        if (startsAt == null || finishesAt == null) {
            return true; // 누락된 시각은 해당 필드의 @NotNull에서 처리합니다.
        }
        return finishesAt.isAfter(startsAt);
    }
}
