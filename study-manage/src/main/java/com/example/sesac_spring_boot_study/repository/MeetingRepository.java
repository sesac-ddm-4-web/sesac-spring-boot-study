package com.example.sesac_spring_boot_study.repository;

import com.example.sesac_spring_boot_study.domain.Meeting;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetingRepository extends JpaRepository<Meeting, Long> {
    List<Meeting> findByStudyIdOrderByStartsAtAscIdAsc(Long studyId);
}
