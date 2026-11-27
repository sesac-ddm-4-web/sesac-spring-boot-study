package com.example.study.dto;

import java.time.DayOfWeek;
import java.time.LocalTime;

import com.example.study.domain.RecruitStatus;
import com.example.study.domain.Study;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.OptBoolean;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateStudyRequest(
        @NotBlank(message = "스터디명을 입력해 주세요.")
        @Size(max = Study.NAME_MAX_LENGTH, message = "스터디명은 50자 이하여야 합니다.")
        String name,

        @NotBlank(message = "상세 내용을 입력해 주세요.")
        @Size(max = Study.DESCRIPTION_MAX_LENGTH, message = "상세 내용은 2000자 이하여야 합니다.")
        String description,

        @NotNull(message = "정원을 입력해 주세요.")
        @Min(value = Study.MIN_CAPACITY, message = "정원은 2명 이상이어야 합니다.")
        @Max(value = Study.MAX_CAPACITY, message = "정원은 100명 이하여야 합니다.")
        Integer capacity,

        @NotNull(message = "모집 상태를 선택해 주세요.")
        RecruitStatus recruitStatus,

        @NotNull(message = "요일을 선택해 주세요.")
        DayOfWeek dayOfWeek,

        @NotNull(message = "시작 시각을 입력해 주세요.")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm", lenient = OptBoolean.FALSE)
        LocalTime startTime,

        @NotNull(message = "종료 시각을 입력해 주세요.")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm", lenient = OptBoolean.FALSE)
        LocalTime endTime) {
}
