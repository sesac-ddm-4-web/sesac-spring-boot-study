package com.example.studyservice.schedule;

import com.example.studyservice.global.exception.ErrorCode;
import com.example.studyservice.schedule.dto.req.ScheduleReq;
import com.example.studyservice.schedule.dto.res.ScheduleRes;
import com.example.studyservice.schedule.service.ScheduleService;
import com.example.studyservice.study.dto.req.StudyCreateReq;
import com.example.studyservice.study.service.ParticipationService;
import com.example.studyservice.study.service.StudyService;
import com.example.studyservice.support.TestMembers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static com.example.studyservice.support.ErrorCodeAssertions.assertErrorCode;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
class ScheduleRuleTest {

    private static final LocalDateTime START = LocalDateTime.of(2026, 12, 3, 19, 0);

    @Autowired
    private StudyService studyService;

    @Autowired
    private ParticipationService participationService;

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private TestMembers testMembers;

    private Long leaderId;
    private Long memberId;
    private Long outsiderId;
    private Long studyId;

    @BeforeEach
    void setUp() {
        leaderId = testMembers.create("leader");
        memberId = testMembers.create("member");
        outsiderId = testMembers.create("outsider");
        studyId = studyService.create(leaderId, new StudyCreateReq("JPA 스터디", null, 4)).id();
        participationService.join(studyId, memberId);
    }

    private ScheduleReq request(String title, LocalDateTime startAt) {
        return new ScheduleReq(title, "3장 연관관계", "강남역", startAt, startAt.plusHours(2));
    }

    @Test
    @DisplayName("스터디장이 등록한 일정은 참여 멤버가 시작 시각 순으로 조회한다.")
    void leaderCreatesMemberReads() {
        scheduleService.create(studyId, leaderId, request("2회차", START.plusDays(7)));
        scheduleService.create(studyId, leaderId, request("1회차", START));

        var schedules = scheduleService.findAll(studyId, memberId);
        assertEquals(2, schedules.size());
        assertEquals("1회차", schedules.get(0).title());
    }

    @Test
    @DisplayName("일반 멤버는 일정을 등록, 수정, 삭제할 수 없다.")
    void memberCannotManage() {
        ScheduleRes created = scheduleService.create(studyId, leaderId, request("1회차", START));

        assertErrorCode(ErrorCode.NOT_STUDY_LEADER,
                () -> scheduleService.create(studyId, memberId, request("몰래", START)));
        assertErrorCode(ErrorCode.NOT_STUDY_LEADER,
                () -> scheduleService.update(studyId, created.id(), memberId, request("변경", START)));
        assertErrorCode(ErrorCode.NOT_STUDY_LEADER,
                () -> scheduleService.delete(studyId, created.id(), memberId));
    }

    @Test
    @DisplayName("참여하지 않은 회원은 일정을 조회할 수 없다.")
    void outsiderCannotRead() {
        assertErrorCode(ErrorCode.NOT_STUDY_MEMBER, () -> scheduleService.findAll(studyId, outsiderId));
    }

    @Test
    @DisplayName("종료 시각은 시작 시각보다 뒤여야 한다.")
    void invalidTime() {
        ScheduleReq invalid = new ScheduleReq("1회차", null, null, START, START);

        assertErrorCode(ErrorCode.INVALID_SCHEDULE_TIME, () -> scheduleService.create(studyId, leaderId, invalid));
    }

    @Test
    @DisplayName("다른 스터디의 일정 번호로는 수정하거나 삭제할 수 없다.")
    void scheduleBelongsToStudy() {
        Long otherStudyId = studyService.create(leaderId, new StudyCreateReq("다른 스터디", null, 4)).id();
        ScheduleRes other = scheduleService.create(otherStudyId, leaderId, request("다른 일정", START));

        assertErrorCode(ErrorCode.SCHEDULE_NOT_FOUND,
                () -> scheduleService.delete(studyId, other.id(), leaderId));
    }
}
