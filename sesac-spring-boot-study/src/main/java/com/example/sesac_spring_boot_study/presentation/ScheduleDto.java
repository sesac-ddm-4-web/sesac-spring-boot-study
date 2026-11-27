package com.example.sesac_spring_boot_study.presentation;

import com.example.sesac_spring_boot_study.domain.Schedule;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class ScheduleDto {
    private ScheduleDto() {
    }

    public record CreateRequest(
            @NotNull Long memberId,
            @NotBlank @Size(max = 100) String title,
            @NotNull @Future LocalDateTime startsAt
    ) {
    }

    public record Response(Long id, Long studyId, String title, LocalDateTime startsAt) {
        public static Response from(Schedule schedule) {
            return new Response(schedule.getId(), schedule.getStudy().getId(),
                    schedule.getTitle(), schedule.getStartsAt());
        }
    }
}
