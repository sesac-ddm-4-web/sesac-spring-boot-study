package com.example.study_member.Repository;

import com.example.study_member.Domain.StudyMeeting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyMeetingRepository extends JpaRepository<StudyMeeting, Long> {
}
