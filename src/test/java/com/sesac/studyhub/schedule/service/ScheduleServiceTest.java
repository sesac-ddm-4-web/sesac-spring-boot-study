package com.sesac.studyhub.schedule.service;

import com.sesac.studyhub.global.exception.ErrorCode;
import com.sesac.studyhub.member.domain.Member;
import com.sesac.studyhub.schedule.domain.Schedule;
import com.sesac.studyhub.schedule.dto.ScheduleCreateRequest;
import com.sesac.studyhub.schedule.dto.ScheduleResponse;
import com.sesac.studyhub.schedule.repository.ScheduleRepository;
import com.sesac.studyhub.study.domain.Study;
import com.sesac.studyhub.study.repository.StudyRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.sesac.studyhub.support.TestFixtures.assertBusinessError;
import static com.sesac.studyhub.support.TestFixtures.member;
import static com.sesac.studyhub.support.TestFixtures.study;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class ScheduleServiceTest {

    @Mock ScheduleRepository scheduleRepository;
    @Mock StudyRepository studyRepository;
    @InjectMocks ScheduleService scheduleService;

    private final Member leader = member(1L, "leader");
    private final Study study = study(10L, leader, 5);
    private final LocalDateTime start = LocalDateTime.now().plusDays(1).withNano(0);
    private final ScheduleCreateRequest request =
        new ScheduleCreateRequest("1주차", "강남", start, start.plusHours(2));

    @Test
    @DisplayName("스터디장은 일정을 등록할 수 있다")
    void leaderCanCreate() {
        given(studyRepository.findById(10L)).willReturn(Optional.of(study));
        given(scheduleRepository.save(any(Schedule.class))).willAnswer(invocation -> invocation.getArgument(0));

        ScheduleResponse response = scheduleService.createSchedule(10L, 1L, request);

        assertThat(response.title()).isEqualTo("1주차");
        assertThat(response.startAt()).isEqualTo(start);
    }

    @Test
    @DisplayName("스터디장이 아니면 NOT_STUDY_LEADER, 저장하지 않는다")
    void nonLeaderCannotCreate() {
        given(studyRepository.findById(10L)).willReturn(Optional.of(study));

        assertBusinessError(() -> scheduleService.createSchedule(10L, 2L, request), ErrorCode.NOT_STUDY_LEADER);
        then(scheduleRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("없는 스터디면 STUDY_NOT_FOUND")
    void studyNotFound() {
        given(studyRepository.findById(999L)).willReturn(Optional.empty());

        assertBusinessError(() -> scheduleService.createSchedule(999L, 1L, request), ErrorCode.STUDY_NOT_FOUND);
    }
}