package com.example.study_member.service;

import com.example.study_member.domain.StudyMeeting;
import com.example.study_member.domain.StudyMembership;
import com.example.study_member.dto.request.CreateStudyMeetingRequest;
import com.example.study_member.dto.response.StudyMeetingResponse;
import com.example.study_member.exception.StudyMembershipNotFoundException;
import com.example.study_member.repository.StudyMeetingRepository;
import com.example.study_member.repository.StudyMembershipRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StudyMeetingService {
    private final StudyMembershipRepository studyMembershipRepository;
    private final StudyMeetingRepository studyMeetingRepository;

    public StudyMeetingService(StudyMembershipRepository studyMembershipRepository, StudyMeetingRepository studyMeetingRepository) {
        this.studyMembershipRepository = studyMembershipRepository;
        this.studyMeetingRepository = studyMeetingRepository;
    }

    @Transactional
    public StudyMeetingResponse createStudyMeeting(Long studyId, CreateStudyMeetingRequest request) {
        StudyMembership membership = studyMembershipRepository.findByStudyIdAndMemberId(
                studyId,
                request.memberId()
        ).orElseThrow(() -> new StudyMembershipNotFoundException(studyId, request.memberId()));

        StudyMeeting meeting = new StudyMeeting(
                membership, request.title(), request.startsAt(),
                request.finishesAt(), request.location(), request.description()
        );

        StudyMeeting savedMeeting = studyMeetingRepository.save(meeting);
        return StudyMeetingResponse.from(savedMeeting);
    }

    @Transactional(readOnly = true)
    public List<StudyMeetingResponse> getStudyMeetingList(Long studyId) {
        return studyMeetingRepository.getAllByStudyId(studyId)
                .stream()
                .map(StudyMeetingResponse::from)
                .toList();
    }

    @Transactional
    public void deleteStudyMeeting(Long meetingId) {
        studyMeetingRepository.deleteById(meetingId);
    }
}
