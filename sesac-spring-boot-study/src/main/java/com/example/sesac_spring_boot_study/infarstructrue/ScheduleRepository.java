package com.example.sesac_spring_boot_study.infarstructrue;

import com.example.sesac_spring_boot_study.domain.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    List<Schedule> findByStudyIdOrderByStartsAtAsc(Long studyId);
}
