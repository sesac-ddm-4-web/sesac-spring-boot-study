package com.example.study.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.example.study.common.exception.BusinessException;
import com.example.study.common.exception.ErrorCode;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.test.util.ReflectionTestUtils;

class StudyScheduleTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 12, 1, 19, 0, 0);

    @ParameterizedTest(name = "{0} → 등록 {1}")
    @CsvSource({
            "2026-12-01T18:59:00, false",
            "2026-12-01T19:00:00, false",
            "2026-12-01T19:00:01, true",
            "2026-12-01T19:01:00, true",
    })
    @DisplayName("현재 시각보다 이후의 일시만 등록할 수 있다")
    void onlyFuture(LocalDateTime meetingAt, boolean allowed) {
        Member owner = new Member("a@example.com", "encoded", "이름", "");
        ReflectionTestUtils.setField(owner, "id", 1L);
        Study study = Study.open(owner, "스터디", "내용", 4, RecruitStatus.RECRUITING,
                DayOfWeek.MONDAY, LocalTime.of(19, 0), LocalTime.of(21, 0));

        if (allowed) {
            Schedule schedule = study.addSchedule(1L, meetingAt, "카페", "모임", NOW);
            assertThat(schedule.getStudy()).isSameAs(study);
        } else {
            assertThatThrownBy(() -> study.addSchedule(1L, meetingAt, "카페", "모임", NOW))
                    .isInstanceOfSatisfying(BusinessException.class,
                            e -> assertThat(e.errorCode()).isEqualTo(ErrorCode.PAST_SCHEDULE));
        }
    }
}
