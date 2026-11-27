package com.example.study.dto;

import com.example.study.domain.RecruitStatus;

import jakarta.validation.constraints.NotNull;

public record RecruitStatusRequest(
        @NotNull(message = "모집 상태를 선택해 주세요.")
        RecruitStatus recruitStatus) {
}
