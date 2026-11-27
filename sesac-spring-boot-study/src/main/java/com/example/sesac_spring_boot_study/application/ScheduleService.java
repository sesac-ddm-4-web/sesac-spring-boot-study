package com.example.sesac_spring_boot_study.application;

import com.example.sesac_spring_boot_study.domain.Schedule;
import com.example.sesac_spring_boot_study.domain.Study;
import com.example.sesac_spring_boot_study.infarstructrue.ScheduleRepository;
import com.example.sesac_spring_boot_study.presentation.ScheduleDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ScheduleService {
    private final ScheduleRepository scheduleRepository;
    private final StudyService studyService;

    public ScheduleService(ScheduleRepository scheduleRepository, StudyService studyService) {
        this.scheduleRepository = scheduleRepository;
        this.studyService = studyService;
    }

    @Transactional
    public ScheduleDto.Response create(Long studyId, Long memberId, String title, LocalDateTime startsAt) {
        Study study = studyService.findStudy(studyId);
        study.validateLeader(memberId);
        Schedule schedule = scheduleRepository.save(new Schedule(study, title, startsAt));
        return ScheduleDto.Response.from(schedule);
    }

    public List<ScheduleDto.Response> findByStudy(Long studyId) {
        studyService.findStudy(studyId);
        return scheduleRepository.findByStudyIdOrderByStartsAtAsc(studyId).stream()
                .map(ScheduleDto.Response::from)
                .toList();
    }
}
