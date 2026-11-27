package com.sesac.studyhub.schedule.domain;

import com.sesac.studyhub.global.exception.ErrorCode;
import com.sesac.studyhub.member.domain.Member;
import com.sesac.studyhub.study.domain.Study;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static com.sesac.studyhub.support.TestFixtures.assertBusinessError;
import static com.sesac.studyhub.support.TestFixtures.member;
import static com.sesac.studyhub.support.TestFixtures.study;
import static org.assertj.core.api.Assertions.assertThat;

class ScheduleTest {

    private final Member leader = member(1L, "leader");
    private final Study study = study(10L, leader, 5);
    private final LocalDateTime start = LocalDateTime.of(2030, 1, 1, 19, 0);

    @Test
    @DisplayName("종료 시간이 시작 시간 이후면 일정이 만들어진다")
    void create() {
        Schedule schedule = Schedule.create(study, "1주차", "강남", start, start.plusHours(2));

        assertThat(schedule.getTitle()).isEqualTo("1주차");
        assertThat(schedule.getStudy()).isEqualTo(study);
    }

    @Test
    @DisplayName("종료 시간이 시작 시간보다 앞서면 INVALID_SCHEDULE_PERIOD")
    void endBeforeStart() {
        assertBusinessError(
            () -> Schedule.create(study, "1주차", null, start, start.minusMinutes(1)),
            ErrorCode.INVALID_SCHEDULE_PERIOD);
    }

    @Test
    @DisplayName("시작과 종료가 같아도 INVALID_SCHEDULE_PERIOD")
    void sameStartAndEnd() {
        assertBusinessError(
            () -> Schedule.create(study, "1주차", null, start, start),
            ErrorCode.INVALID_SCHEDULE_PERIOD);
    }
}