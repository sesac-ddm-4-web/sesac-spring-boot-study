package com.sesac.studyhub.schedule.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record ScheduleCreateRequest(
    @NotBlank(message = "일정 제목은 필수입니다.")
    @Size(max = 100, message = "일정 제목은 100자 이하여야 합니다.")
    String title,

    @Size(max = 100, message = "장소는 100자 이하여야 합니다.")
    String place,

    @NotNull(message = "시작 시간은 필수입니다.")
    @Future(message = "시작 시간은 현재 이후여야 합니다.")
    LocalDateTime startAt,

    @NotNull(message = "종료 시간은 필수입니다.")
    LocalDateTime endAt
) { }