package com.example.study.dto;

import java.time.LocalDateTime;

import com.example.study.domain.Schedule;
import com.fasterxml.jackson.annotation.JsonFormat;

public record ScheduleResponse(
        Long id,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "uuuu-MM-dd'T'HH:mm") LocalDateTime meetingAt,
        String place,
        String content) {

    public static ScheduleResponse from(Schedule schedule) {
        return new ScheduleResponse(schedule.getId(), schedule.getMeetingAt(), schedule.getPlace(),
                schedule.getContent());
    }
}
