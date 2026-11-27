package com.example.study_member.repository;

import com.example.study_member.domain.StudyMeeting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudyMeetingRepository extends JpaRepository<StudyMeeting, Long> {
    List<StudyMeeting> getAllByStudyId(Long studyId);
}
