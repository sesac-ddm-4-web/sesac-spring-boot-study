package com.example.studyservice.schedule.service;

import com.example.studyservice.global.exception.ApiException;
import com.example.studyservice.global.exception.ErrorCode;
import com.example.studyservice.schedule.domain.Schedule;
import com.example.studyservice.schedule.dto.req.ScheduleReq;
import com.example.studyservice.schedule.dto.res.ScheduleRes;
import com.example.studyservice.schedule.repository.ScheduleRepository;
import com.example.studyservice.study.domain.Study;
import com.example.studyservice.study.service.StudyReader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final StudyReader studyReader;
    private final Clock clock;

    public ScheduleService(ScheduleRepository scheduleRepository, StudyReader studyReader, Clock clock) {
        this.scheduleRepository = scheduleRepository;
        this.studyReader = studyReader;
        this.clock = clock;
    }

    @Transactional
    public ScheduleRes create(Long studyId, Long memberId, ScheduleReq request) {
        Study study = studyReader.findStudy(studyId);
        studyReader.requireLeader(studyId, memberId);
        study.validateActive();
        Schedule schedule = new Schedule(study, request.title(), request.description(), request.location(),
                request.startAt(), request.endAt(), LocalDateTime.now(clock));
        return ScheduleRes.of(scheduleRepository.save(schedule));
    }

    public List<ScheduleRes> findAll(Long studyId, Long memberId) {
        studyReader.findStudy(studyId);
        studyReader.requireActiveMember(studyId, memberId);
        return scheduleRepository.findByStudyId(studyId).stream()
                .map(ScheduleRes::of)
                .toList();
    }

    public ScheduleRes findOne(Long studyId, Long scheduleId, Long memberId) {
        studyReader.findStudy(studyId);
        studyReader.requireActiveMember(studyId, memberId);
        return ScheduleRes.of(findSchedule(studyId, scheduleId));
    }

    @Transactional
    public ScheduleRes update(Long studyId, Long scheduleId, Long memberId, ScheduleReq request) {
        Study study = studyReader.findStudy(studyId);
        studyReader.requireLeader(studyId, memberId);
        study.validateActive();
        Schedule schedule = findSchedule(studyId, scheduleId);
        schedule.update(request.title(), request.description(), request.location(),
                request.startAt(), request.endAt());
        return ScheduleRes.of(schedule);
    }

    @Transactional
    public void delete(Long studyId, Long scheduleId, Long memberId) {
        Study study = studyReader.findStudy(studyId);
        studyReader.requireLeader(studyId, memberId);
        study.validateActive();
        scheduleRepository.delete(findSchedule(studyId, scheduleId));
    }

    private Schedule findSchedule(Long studyId, Long scheduleId) {
        return scheduleRepository.findByIdAndStudyId(scheduleId, studyId)
                .orElseThrow(() -> new ApiException(ErrorCode.SCHEDULE_NOT_FOUND, scheduleId));
    }
}
