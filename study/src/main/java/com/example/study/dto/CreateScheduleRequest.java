package com.example.study.dto;

import java.time.LocalDateTime;

import com.example.study.domain.Schedule;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.OptBoolean;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateScheduleRequest(
        @NotNull(message = "일시를 입력해 주세요.")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "uuuu-MM-dd'T'HH:mm", lenient = OptBoolean.FALSE)
        LocalDateTime meetingAt,

        @NotBlank(message = "장소를 입력해 주세요.")
        @Size(max = Schedule.PLACE_MAX_LENGTH, message = "장소는 100자 이하여야 합니다.")
        String place,

        @NotBlank(message = "내용을 입력해 주세요.")
        @Size(max = Schedule.CONTENT_MAX_LENGTH, message = "내용은 1000자 이하여야 합니다.")
        String content) {
}
