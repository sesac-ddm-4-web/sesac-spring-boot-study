package com.example.study_member.dto.response;

import com.example.study_member.domain.StudyMeeting;

import java.time.LocalDateTime;

public record StudyMeetingResponse(
        Long meetingId,
        Long studyId,
        Long creatorId,
        String title,
        LocalDateTime startsAt,
        LocalDateTime finishesAt,
        String location,
        String description) {

    public static StudyMeetingResponse from(StudyMeeting meeting) {
        return new StudyMeetingResponse(
                meeting.getId(),
                meeting.getStudy().getId(),
                meeting.getCreator().getId(),
                meeting.getTitle(),
                meeting.getStartsAt(),
                meeting.getFinishesAt(),
                meeting.getLocation(),
                meeting.getDescription()
        );
    }
}
