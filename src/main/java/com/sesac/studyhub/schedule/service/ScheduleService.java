package com.sesac.studyhub.schedule.service;

import com.sesac.studyhub.global.exception.BusinessException;
import com.sesac.studyhub.global.exception.ErrorCode;
import com.sesac.studyhub.schedule.domain.Schedule;
import com.sesac.studyhub.schedule.dto.ScheduleCreateRequest;
import com.sesac.studyhub.schedule.dto.ScheduleResponse;
import com.sesac.studyhub.schedule.repository.ScheduleRepository;
import com.sesac.studyhub.study.domain.Study;
import com.sesac.studyhub.study.repository.StudyRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final StudyRepository studyRepository;

    @Transactional
    public ScheduleResponse createSchedule(Long studyId, Long memberId, ScheduleCreateRequest request) {
        Study study = studyRepository.findById(studyId)
            .orElseThrow(() -> new BusinessException(ErrorCode.STUDY_NOT_FOUND));

        if (!study.isLeader(memberId)) {
            throw new BusinessException(ErrorCode.NOT_STUDY_LEADER);
        }

        Schedule schedule = Schedule.create(
            study, request.title(), request.place(), request.startAt(), request.endAt()
        );
        return ScheduleResponse.from(scheduleRepository.save(schedule));
    }

    public List<ScheduleResponse> getSchedules(Long studyId) {
        if (!studyRepository.existsById(studyId)) {
            throw new BusinessException(ErrorCode.STUDY_NOT_FOUND);
        }
        return scheduleRepository.findAllByStudyIdOrderByStartAtAsc(studyId)
            .stream()
            .map(ScheduleResponse::from)
            .toList();
    }

}
