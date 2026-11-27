package com.example.study.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.DayOfWeek;
import java.time.LocalTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class StudyTest {

    @ParameterizedTest(name = "{0} {1}~{2} → 겹침 {3}")
    @CsvSource({
            "MONDAY,  19:00, 21:00, true",
            "MONDAY,  18:00, 19:01, true",
            "MONDAY,  20:59, 22:00, true",
            "MONDAY,  19:30, 20:00, true",
            "MONDAY,  18:00, 22:00, true",
            "MONDAY,  17:00, 19:00, false",
            "MONDAY,  21:00, 23:00, false",
            "MONDAY,  08:00, 09:00, false",
            "TUESDAY, 19:00, 21:00, false",
    })
    @DisplayName("월 19~21시와의 겹침 판정 (맞닿는 시간은 허용)")
    void overlaps(DayOfWeek day, LocalTime start, LocalTime end, boolean expected) {
        Study existing = study(DayOfWeek.MONDAY, LocalTime.of(19, 0), LocalTime.of(21, 0));
        Study candidate = study(day, start, end);

        assertThat(candidate.overlaps(existing)).isEqualTo(expected);
        assertThat(existing.overlaps(candidate)).isEqualTo(expected);
    }

    private Study study(DayOfWeek day, LocalTime start, LocalTime end) {
        Member owner = new Member("a@example.com", "encoded", "이름", "");
        return Study.open(owner, "스터디", "내용", 4, RecruitStatus.RECRUITING, day, start, end);
    }
}
