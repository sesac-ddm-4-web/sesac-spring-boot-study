package com.sesac.studyhub.schedule.repository;

import com.sesac.studyhub.schedule.domain.Schedule;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    List<Schedule> findAllByStudyIdOrderByStartAtAsc(Long studyId);

}
