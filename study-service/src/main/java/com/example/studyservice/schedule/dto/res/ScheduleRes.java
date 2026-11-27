package com.example.studyservice.schedule.dto.res;

import com.example.studyservice.schedule.domain.Schedule;

import java.time.LocalDateTime;

public record ScheduleRes(
        Long id,
        Long studyId,
        String title,
        String description,
        String location,
        LocalDateTime startAt,
        LocalDateTime endAt,
        LocalDateTime createdAt
) {
    public static ScheduleRes of(Schedule schedule) {
        return new ScheduleRes(schedule.getId(), schedule.getStudy().getId(), schedule.getTitle(),
                schedule.getDescription(), schedule.getLocation(), schedule.getStartAt(), schedule.getEndAt(),
                schedule.getCreatedAt());
    }
}
