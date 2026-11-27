package com.example.study.dto;

import java.time.DayOfWeek;
import java.time.LocalTime;

import com.example.study.domain.RecruitStatus;
import com.fasterxml.jackson.annotation.JsonFormat;

public record StudySummaryResponse(
        Long id,
        String name,
        String ownerName,
        int capacity,
        long memberCount,
        RecruitStatus recruitStatus,
        DayOfWeek dayOfWeek,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm") LocalTime startTime,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm") LocalTime endTime) {
}
