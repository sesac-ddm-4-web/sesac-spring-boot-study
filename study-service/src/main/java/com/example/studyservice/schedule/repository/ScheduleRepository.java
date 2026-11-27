package com.example.studyservice.schedule.repository;

import com.example.studyservice.schedule.domain.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    @Query("select s from Schedule s where s.study.id = :studyId order by s.startAt asc, s.id asc")
    List<Schedule> findByStudyId(Long studyId);

    @Query("select s from Schedule s where s.id = :scheduleId and s.study.id = :studyId")
    Optional<Schedule> findByIdAndStudyId(Long scheduleId, Long studyId);
}
