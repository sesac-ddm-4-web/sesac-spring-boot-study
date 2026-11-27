package com.example.personal_project.study.application;

import com.example.personal_project.member.application.MemberService;
import com.example.personal_project.study.domain.Study;
import com.example.personal_project.study.domain.StudyMeeting;
import com.example.personal_project.study.repository.StudyMeetingRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class StudyMeetingService {

    private final StudyService studies;
    private final StudyMembershipService memberships;
    private final StudyMeetingRepository meetings;
    private final MemberService members;

    public StudyMeetingService(StudyService studies, StudyMembershipService memberships, StudyMeetingRepository meetings, MemberService members) {
        this.studies = studies;
        this.memberships = memberships;
        this.meetings = meetings;
        this.members = members;
    }

    @Transactional
    public Meeting create(long studyId, long actorId, String title, String location, Instant startsAt, Instant endsAt) {
        members.requireMember(actorId);
        Study study = studies.requireStudy(studyId);
        studies.requireOwner(studyId, actorId);
        StudyMeeting meeting = study.addMeeting(title, location, startsAt, endsAt, Instant.now());
        return Meeting.from(meetings.save(meeting));
    }

    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public Page<Meeting> list(long studyId, long actorId, Pageable page) {
        members.requireMember(actorId);
        studies.requireStudy(studyId);
        memberships.requireActiveMembership(studyId, actorId);
        return meetings.findByStudy_IdOrderByStartsAtAscIdAsc(studyId, page).map(Meeting::from);
    }

    public record Meeting(long meetingId, long studyId, String title, String location, Instant startsAt, Instant endsAt, Instant createdAt) {

        private static Meeting from(StudyMeeting meeting) {
            return new Meeting(meeting.getId(), meeting.getStudy().getId(), meeting.getTitle(), meeting.getLocation(),
                    meeting.getStartsAt(), meeting.getEndsAt(), meeting.getCreatedAt());
        }
    }
}
