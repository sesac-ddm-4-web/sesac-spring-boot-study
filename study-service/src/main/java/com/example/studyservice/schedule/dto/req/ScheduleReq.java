package com.example.studyservice.schedule.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record ScheduleReq(
        @NotBlank
        @Size(max = 50)
        String title,

        @Size(max = 500)
        String description,

        @Size(max = 100)
        String location,

        @NotNull
        LocalDateTime startAt,

        @NotNull
        LocalDateTime endAt
) {
}
