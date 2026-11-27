package com.sesac.studyhub.schedule.dto;

import com.sesac.studyhub.schedule.domain.Schedule;

import java.time.LocalDateTime;

public record ScheduleResponse(
    Long id,
    String title,
    String place,
    LocalDateTime startAt,
    LocalDateTime endAt,
    LocalDateTime createdAt
) {
    public static ScheduleResponse from(Schedule schedule) {
        return new ScheduleResponse(
            schedule.getId(), schedule.getTitle(), schedule.getPlace(),
            schedule.getStartAt(), schedule.getEndAt(), schedule.getCreatedAt());
    }
}