package com.example.study.repository;

import java.util.List;

import com.example.study.domain.Schedule;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    /** 지난 일정을 포함해 일시 오름차순. 같은 일시는 등록한 순서다. */
    List<Schedule> findByStudyIdOrderByMeetingAtAscIdAsc(Long studyId);
}
