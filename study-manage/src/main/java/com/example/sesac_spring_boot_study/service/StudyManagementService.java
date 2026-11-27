package com.example.sesac_spring_boot_study.service;

import com.example.sesac_spring_boot_study.domain.Member;
import com.example.sesac_spring_boot_study.domain.Profile;
import com.example.sesac_spring_boot_study.domain.StudyGroup;
import com.example.sesac_spring_boot_study.domain.Participation;
import com.example.sesac_spring_boot_study.domain.Meeting;
import com.example.sesac_spring_boot_study.repository.MemberRepository;
import com.example.sesac_spring_boot_study.repository.ProfileRepository;
import com.example.sesac_spring_boot_study.repository.StudyGroupRepository;
import com.example.sesac_spring_boot_study.repository.ParticipationRepository;
import com.example.sesac_spring_boot_study.repository.MeetingRepository;

import java.time.Instant;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = true)
public class StudyManagementService {
    private final MemberRepository members;
    private final ProfileRepository profiles;
    private final StudyGroupRepository studies;
    private final ParticipationRepository participations;
    private final MeetingRepository meetings;

    StudyManagementService(MemberRepository members, ProfileRepository profiles, StudyGroupRepository studies,
                           ParticipationRepository participations, MeetingRepository meetings) {
        this.members = members;
        this.profiles = profiles;
        this.studies = studies;
        this.participations = participations;
        this.meetings = meetings;
    }

    @Transactional
    public MemberView createMember(String name, String bio) {
        Member member = members.save(new Member(name));
        return memberView(profiles.save(new Profile(member, bio)));
    }

    public MemberView getMember(long id) {
        return memberView(profile(id));
    }

    @Transactional
    public MemberView updateMember(long id, String name, String bio) {
        Profile profile = profile(id);
        profile.getMember().rename(name);
        profile.updateBio(bio);
        return memberView(profile);
    }

    @Transactional
    public StudyView createStudy(long leaderId, String title, String description, int capacity) {
        Member leader = member(leaderId);
        StudyGroup study = studies.save(new StudyGroup(leader, title, description, capacity));
        participations.save(new Participation(study, leader));
        return studyView(study);
    }

    public List<StudyView> listStudies() {
        return studies.findAllByOrderByIdAsc().stream().map(this::studyView).toList();
    }

    public StudyView getStudy(long id) {
        return studyView(study(id));
    }

    @Transactional
    public ParticipantView join(long studyId, long memberId) {
        StudyGroup study = lockedStudy(studyId);
        Member member = member(memberId);
        if (participations.findByStudyIdAndMemberId(studyId, memberId).isPresent()) {
            throw conflict("이미 참여 중인 회원입니다.");
        }
        if (participations.countByStudyId(studyId) >= study.getCapacity()) {
            throw conflict("스터디 정원이 찼습니다.");
        }
        return participantView(participations.save(new Participation(study, member)));
    }

    @Transactional
    public void leave(long studyId, long memberId) {
        StudyGroup study = lockedStudy(studyId);
        if (study.getLeader().getId().equals(memberId)) {
            throw conflict("스터디장은 탈퇴할 수 없습니다.");
        }
        Participation participation = participations.findByStudyIdAndMemberId(studyId, memberId)
                .orElseThrow(() -> notFound("참여 정보를 찾을 수 없습니다."));
        participations.delete(participation);
    }

    public List<ParticipantView> listParticipants(long studyId) {
        study(studyId);
        return participations.findByStudyIdOrderByJoinedAtAscIdAsc(studyId).stream()
                .map(this::participantView)
                .toList();
    }

    @Transactional
    public MeetingView createMeeting(long studyId, long memberId, String title, String location, Instant startsAt) {
        StudyGroup study = study(studyId);
        if (!study.getLeader().getId().equals(memberId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "스터디장만 일정을 등록할 수 있습니다.");
        }
        return meetingView(meetings.save(new Meeting(study, title, location, startsAt)));
    }

    public List<MeetingView> listMeetings(long studyId) {
        study(studyId);
        return meetings.findByStudyIdOrderByStartsAtAscIdAsc(studyId).stream().map(this::meetingView).toList();
    }

    private Member member(long id) {
        return members.findById(id).orElseThrow(() -> notFound("회원을 찾을 수 없습니다."));
    }

    private Profile profile(long id) {
        return profiles.findByMemberId(id).orElseThrow(() -> notFound("회원 프로필을 찾을 수 없습니다."));
    }

    private StudyGroup study(long id) {
        return studies.findById(id).orElseThrow(() -> notFound("스터디를 찾을 수 없습니다."));
    }

    private StudyGroup lockedStudy(long id) {
        return studies.findForUpdate(id).orElseThrow(() -> notFound("스터디를 찾을 수 없습니다."));
    }

    private MemberView memberView(Profile profile) {
        return new MemberView(profile.getMember().getId(), profile.getMember().getName(), profile.getBio());
    }

    private StudyView studyView(StudyGroup study) {
        return new StudyView(study.getId(), study.getLeader().getId(), study.getTitle(), study.getDescription(), study.getCapacity());
    }

    private ParticipantView participantView(Participation participation) {
        return new ParticipantView(participation.getMember().getId(), participation.getMember().getName(), participation.getJoinedAt());
    }

    private MeetingView meetingView(Meeting meeting) {
        return new MeetingView(meeting.getId(), meeting.getTitle(), meeting.getLocation(), meeting.getStartsAt());
    }

    private ResponseStatusException notFound(String message) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
    }

    private ResponseStatusException conflict(String message) {
        return new ResponseStatusException(HttpStatus.CONFLICT, message);
    }

    public record MemberView(long id, String name, String bio) {}
    public record StudyView(long id, long leaderId, String title, String description, int capacity) {}
    public record ParticipantView(long memberId, String name, Instant joinedAt) {}
    public record MeetingView(long id, String title, String location, Instant startsAt) {}
}
