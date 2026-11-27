package com.example.study.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import com.example.study.common.exception.BusinessException;
import com.example.study.common.exception.ErrorCode;
import com.example.study.domain.Schedule;
import com.example.study.domain.Study;
import com.example.study.dto.CreateScheduleRequest;
import com.example.study.dto.ScheduleResponse;
import com.example.study.repository.ScheduleRepository;
import com.example.study.repository.StudyRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ScheduleService {

    private final StudyRepository studyRepository;
    private final ScheduleRepository scheduleRepository;
    private final Clock clock;

    public ScheduleService(StudyRepository studyRepository, ScheduleRepository scheduleRepository, Clock clock) {
        this.studyRepository = studyRepository;
        this.scheduleRepository = scheduleRepository;
        this.clock = clock;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ScheduleResponse register(Long memberId, Long studyId, CreateScheduleRequest request) {
        // 삭제와 순서가 정해지도록 스터디를 잠근다. 이후 회원 행은 잠그지 않아 잠금 순서에 어긋나지 않는다.
        Study study = studyRepository.findByIdForUpdate(studyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDY_NOT_FOUND));
        // 현재 시각은 잠금을 얻은 뒤에 읽는다.
        Schedule schedule = study.addSchedule(memberId, request.meetingAt(), request.place(),
                request.content(), LocalDateTime.now(clock));
        scheduleRepository.save(schedule);
        return ScheduleResponse.from(schedule);
    }

    public List<ScheduleResponse> getSchedules(Long memberId, Long studyId) {
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDY_NOT_FOUND));
        study.validateScheduleReader(memberId);
        return scheduleRepository.findByStudyIdOrderByMeetingAtAscIdAsc(studyId).stream()
                .map(ScheduleResponse::from)
                .toList();
    }
}
