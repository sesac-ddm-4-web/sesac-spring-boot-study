package com.example.personal_project.study.repository;

import com.example.personal_project.study.domain.StudyMeeting;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyMeetingRepository extends JpaRepository<StudyMeeting, Long> {

    Page<StudyMeeting> findByStudy_IdOrderByStartsAtAscIdAsc(long studyId, Pageable page);
}
