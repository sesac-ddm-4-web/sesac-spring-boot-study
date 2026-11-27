package com.example.study.dto;

import java.time.DayOfWeek;
import java.time.LocalTime;

import com.example.study.domain.RecruitStatus;
import com.example.study.domain.Study;
import com.example.study.domain.StudyRole;
import com.fasterxml.jackson.annotation.JsonFormat;

public record StudyDetailResponse(
        Long id,
        String name,
        String description,
        String ownerName,
        int capacity,
        long memberCount,
        RecruitStatus recruitStatus,
        DayOfWeek dayOfWeek,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm") LocalTime startTime,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm") LocalTime endTime,
        StudyRole myRole) {

    /** 스터디장과 참여자가 지연 로딩이므로 트랜잭션 안에서 호출한다. */
    public static StudyDetailResponse of(Study study, Long viewerId) {
        return new StudyDetailResponse(
                study.getId(),
                study.getName(),
                study.getDescription(),
                study.getOwner().getName(),
                study.getCapacity(),
                study.getMembers().size(),
                study.getRecruitStatus(),
                study.getDayOfWeek(),
                study.getStartTime(),
                study.getEndTime(),
                study.roleOf(viewerId));
    }
}
