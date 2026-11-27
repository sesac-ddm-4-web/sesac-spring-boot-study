package com.example.studyservice.study;

import com.example.studyservice.global.exception.ErrorCode;
import com.example.studyservice.study.domain.ParticipationStatus;
import com.example.studyservice.study.domain.StudyMember;
import com.example.studyservice.study.domain.StudyRole;
import com.example.studyservice.study.domain.StudyStatus;
import com.example.studyservice.study.dto.req.StudyCreateReq;
import com.example.studyservice.study.dto.req.StudyUpdateReq;
import com.example.studyservice.study.dto.res.StudyDetailRes;
import com.example.studyservice.study.repository.StudyMemberRepository;
import com.example.studyservice.study.service.ParticipationService;
import com.example.studyservice.study.service.StudyService;
import com.example.studyservice.support.TestMembers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static com.example.studyservice.support.ErrorCodeAssertions.assertErrorCode;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
@Transactional
class ParticipationRuleTest {

    @Autowired
    private StudyService studyService;

    @Autowired
    private ParticipationService participationService;

    @Autowired
    private StudyMemberRepository studyMemberRepository;

    @Autowired
    private TestMembers testMembers;

    private Long leaderId;
    private Long aliceId;
    private Long bobId;

    @BeforeEach
    void setUp() {
        leaderId = testMembers.create("leader");
        aliceId = testMembers.create("alice");
        bobId = testMembers.create("bob");
    }

    private Long createStudy(int capacity) {
        return studyService.create(leaderId, new StudyCreateReq("JPA 스터디", "연관관계 공부", capacity)).id();
    }

    @Test
    @DisplayName("스터디를 만들면 개설자가 스터디장으로 참여하고 인원은 1명이다.")
    void creatorBecomesLeader() {
        StudyDetailRes study = studyService.create(leaderId, new StudyCreateReq("JPA 스터디", null, 4));

        assertEquals(1, study.memberCount());
        assertEquals(leaderId, study.leader().memberId());
        assertEquals(StudyRole.LEADER, participationService.findMembers(study.id()).get(0).role());
    }

    @Test
    @DisplayName("이미 참여 중인 스터디에 다시 참여하면 ALREADY_JOINED")
    void duplicateJoin() {
        Long studyId = createStudy(4);
        participationService.join(studyId, aliceId);

        assertErrorCode(ErrorCode.ALREADY_JOINED, () -> participationService.join(studyId, aliceId));
        assertErrorCode(ErrorCode.ALREADY_JOINED, () -> participationService.join(studyId, leaderId));
    }

    @Test
    @DisplayName("정원이 찬 스터디에는 참여할 수 없다.")
    void capacityFull() {
        Long studyId = createStudy(2);
        participationService.join(studyId, aliceId);

        assertErrorCode(ErrorCode.STUDY_FULL, () -> participationService.join(studyId, bobId));
        assertEquals(2, studyService.findDetail(studyId).memberCount());
    }

    @Test
    @DisplayName("탈퇴하면 기록은 LEFT로 남고, 재가입하면 같은 행이 일반 멤버로 돌아온다.")
    void leaveAndRejoin() {
        Long studyId = createStudy(4);
        participationService.join(studyId, aliceId);
        participationService.leave(studyId, aliceId);

        StudyMember left = studyMemberRepository.findByStudyIdAndMemberId(studyId, aliceId).orElseThrow();
        assertEquals(ParticipationStatus.LEFT, left.getStatus());
        assertEquals(1, studyService.findDetail(studyId).memberCount());

        participationService.join(studyId, aliceId);

        StudyMember rejoined = studyMemberRepository.findByStudyIdAndMemberId(studyId, aliceId).orElseThrow();
        assertEquals(left.getId(), rejoined.getId());
        assertEquals(ParticipationStatus.ACTIVE, rejoined.getStatus());
        assertNull(rejoined.getLeftAt());
        assertEquals(2, studyService.findDetail(studyId).memberCount());
    }

    @Test
    @DisplayName("참여하지 않은 스터디는 탈퇴할 수 없다.")
    void leaveWithoutJoin() {
        Long studyId = createStudy(4);

        assertErrorCode(ErrorCode.NOT_STUDY_MEMBER, () -> participationService.leave(studyId, aliceId));
    }

    @Test
    @DisplayName("다른 멤버가 있으면 스터디장은 위임 전에 탈퇴할 수 없고, 위임 후에는 탈퇴할 수 있다.")
    void leaderMustDelegateBeforeLeaving() {
        Long studyId = createStudy(4);
        participationService.join(studyId, aliceId);

        assertErrorCode(ErrorCode.LEADER_MUST_DELEGATE, () -> participationService.leave(studyId, leaderId));

        participationService.delegateLeader(studyId, leaderId, aliceId);
        participationService.leave(studyId, leaderId);

        StudyDetailRes study = studyService.findDetail(studyId);
        assertEquals(aliceId, study.leader().memberId());
        assertEquals(1, study.memberCount());
        assertEquals(StudyStatus.ACTIVE, study.status());
    }

    @Test
    @DisplayName("혼자 남은 스터디장이 탈퇴하면 스터디가 종료되고 더 이상 참여할 수 없다.")
    void lastLeaderLeavingClosesStudy() {
        Long studyId = createStudy(4);
        participationService.leave(studyId, leaderId);

        StudyDetailRes study = studyService.findDetail(studyId);
        assertEquals(StudyStatus.CLOSED, study.status());
        assertEquals(0, study.memberCount());
        assertNull(study.leader());
        assertErrorCode(ErrorCode.STUDY_CLOSED, () -> participationService.join(studyId, aliceId));
    }

    @Test
    @DisplayName("위임은 스터디장만, 참여 중인 다른 멤버에게만 할 수 있다.")
    void delegateRules() {
        Long studyId = createStudy(4);
        participationService.join(studyId, aliceId);

        assertErrorCode(ErrorCode.NOT_STUDY_LEADER,
                () -> participationService.delegateLeader(studyId, aliceId, leaderId));
        assertErrorCode(ErrorCode.CANNOT_DELEGATE_TO_SELF,
                () -> participationService.delegateLeader(studyId, leaderId, leaderId));
        assertErrorCode(ErrorCode.DELEGATE_TARGET_NOT_MEMBER,
                () -> participationService.delegateLeader(studyId, leaderId, bobId));
    }

    @Test
    @DisplayName("스터디 수정은 스터디장만 가능하고, 정원을 현재 인원보다 적게 줄일 수 없다.")
    void updateRules() {
        Long studyId = createStudy(4);
        participationService.join(studyId, aliceId);
        participationService.join(studyId, bobId);

        assertErrorCode(ErrorCode.NOT_STUDY_LEADER,
                () -> studyService.update(studyId, aliceId, new StudyUpdateReq("변경", null, 5)));
        assertErrorCode(ErrorCode.CAPACITY_BELOW_MEMBER_COUNT,
                () -> studyService.update(studyId, leaderId, new StudyUpdateReq("변경", null, 2)));

        StudyDetailRes updated = studyService.update(studyId, leaderId, new StudyUpdateReq("변경", "설명", 3));
        assertEquals(3, updated.capacity());
        assertErrorCode(ErrorCode.STUDY_FULL, () -> participationService.join(studyId, testMembers.create("carol")));
    }

    @Test
    @DisplayName("내 스터디 목록에는 참여 중인 스터디만 역할과 함께 나온다.")
    void myStudies() {
        Long first = createStudy(4);
        Long second = createStudy(4);
        participationService.join(first, aliceId);
        participationService.join(second, aliceId);
        participationService.leave(second, aliceId);

        var myStudies = participationService.findMyStudies(aliceId);
        assertEquals(1, myStudies.size());
        assertEquals(first, myStudies.get(0).studyId());
        assertEquals(StudyRole.MEMBER, myStudies.get(0).myRole());
    }
}
