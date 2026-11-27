package com.example.studyservice.study.dto.req;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record StudyCreateReq(
        @NotBlank
        @Size(max = 50)
        String title,

        @Size(max = 1000)
        String description,

        @NotNull
        @Min(2)
        @Max(50)
        Integer capacity
) {
}
