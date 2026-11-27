package com.sesac.studyhub.study.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record StudyCreateRequest(
    @NotBlank(message = "스터디 이름은 필수입니다.")
    @Size(max = 50, message = "스터디 이름은 50자 이하여야 합니다.")
    String name,

    @Size(max = 1000, message = "스터디 소개는 1000자 이하여야 합니다.")
    String description,

    @NotNull(message = "정원은 필수입니다.")
    @Min(value = 2, message = "정원은 2명 이상이어야 합니다.")
    @Max(value = 50, message = "정원은 50명 이하여야 합니다.")
    Integer capacity
) { }