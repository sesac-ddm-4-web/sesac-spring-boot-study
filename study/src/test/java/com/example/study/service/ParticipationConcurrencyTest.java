package com.example.study.service;

import static com.example.study.service.ConcurrencyTestSupport.runTogether;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.example.study.DatabaseCleaner;
import com.example.study.IntegrationTest;
import com.example.study.common.exception.BusinessException;
import com.example.study.common.exception.ErrorCode;
import com.example.study.domain.RecruitStatus;
import com.example.study.dto.CreateStudyRequest;
import com.example.study.dto.RecruitStatusRequest;
import com.example.study.dto.SignupRequest;
import com.example.study.repository.StudyRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@IntegrationTest
class ParticipationConcurrencyTest {

    @Autowired
    MemberService memberService;
    @Autowired
    StudyService studyService;
    @Autowired
    StudyParticipationService participationService;
    @Autowired
    StudyDeletionService deletionService;
    @Autowired
    StudyRepository studyRepository;
    @Autowired
    PlatformTransactionManager transactionManager;
    @Autowired
    JdbcTemplate jdbcTemplate;
    @Autowired
    DatabaseCleaner databaseCleaner;

    @BeforeEach
    void clean() {
        databaseCleaner.clean();
    }

    @RepeatedTest(10)
    @DisplayName("마지막 한 자리에 여러 명이 신청해도 한 명만 참여한다")
    void lastSeat() throws Exception {
        Long studyId = open(member("owner"), 2, DayOfWeek.MONDAY);
        List<Callable<?>> tasks = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            Long applicant = member("applicant" + i);
            tasks.add(() -> participationService.join(applicant, studyId));
        }

        var outcomes = runTogether(tasks);

        assertThat(outcomes).filteredOn(o -> o.success()).hasSize(1);
        assertThat(outcomes).filteredOn(o -> o.error() == ErrorCode.STUDY_FULL).hasSize(5);
        assertThat(count("study_member")).isEqualTo(2);
    }

    @RepeatedTest(5)
    @DisplayName("같은 회원의 동시 참여 신청은 한 번만 성공한다")
    void sameStudyTwice() throws Exception {
        Long studyId = open(member("owner"), 10, DayOfWeek.MONDAY);
        Long member = member("member");
        Callable<?> join = () -> participationService.join(member, studyId);

        var outcomes = runTogether(List.of(join, join, join, join));

        assertThat(outcomes).filteredOn(o -> o.success()).hasSize(1);
        assertThat(outcomes).filteredOn(o -> o.error() == ErrorCode.ALREADY_JOINED).hasSize(3);
    }

    @RepeatedTest(10)
    @DisplayName("겹치는 스터디 두 곳의 참여와 같은 시간의 개설을 동시에 하면 하나만 성공한다")
    void joinAndOpenAtOverlappingTime() throws Exception {
        Long first = open(member("ownerA"), 10, DayOfWeek.MONDAY);
        Long second = open(member("ownerB"), 10, DayOfWeek.MONDAY);
        Long member = member("member");

        var outcomes = runTogether(List.of(
                () -> participationService.join(member, first),
                () -> participationService.join(member, second),
                () -> studyService.open(member, request(10, DayOfWeek.MONDAY))));

        assertThat(outcomes).filteredOn(o -> o.success()).hasSize(1);
        assertThat(outcomes).filteredOn(o -> o.error() == ErrorCode.TIME_OVERLAP).hasSize(2);
        assertThat(count("study_member where member_id = " + member)).isEqualTo(1);
    }

    @RepeatedTest(10)
    @DisplayName("삭제와 참여가 겹치면, 참여에 성공한 회원은 반드시 삭제 이력을 받는다")
    void deleteWhileJoining() throws Exception {
        Long owner = member("owner");
        Long studyId = open(owner, 10, DayOfWeek.MONDAY);
        List<Callable<?>> tasks = new ArrayList<>();
        tasks.add(() -> delete(owner, studyId));
        for (int i = 0; i < 5; i++) {
            Long applicant = member("applicant" + i);
            tasks.add(() -> participationService.join(applicant, studyId));
        }

        var outcomes = runTogether(tasks);

        assertThat(outcomes.getFirst().success()).isTrue();
        var joins = outcomes.subList(1, outcomes.size());
        assertThat(joins).allMatch(o -> o.success() || o.error() == ErrorCode.STUDY_NOT_FOUND);
        long joined = joins.stream().filter(o -> o.success()).count();
        assertThat(count("study_history where reason = 'STUDY_DELETED'")).isEqualTo(joined);
        assertThat(count("study_history where reason = 'OWNER_DELETED'")).isEqualTo(1);
        assertThat(count("study_member")).isZero();
    }

    @RepeatedTest(10)
    @DisplayName("삭제와 탈퇴가 겹치면, 각 참여자의 이력은 LEFT 또는 STUDY_DELETED 한 건이다")
    void deleteWhileLeaving() throws Exception {
        Long owner = member("owner");
        Long studyId = open(owner, 10, DayOfWeek.MONDAY);
        List<Long> participants = new ArrayList<>();
        List<Callable<?>> tasks = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Long participant = member("participant" + i);
            participationService.join(participant, studyId);
            participants.add(participant);
        }
        tasks.add(() -> delete(owner, studyId));
        participants.forEach(id -> tasks.add(() -> leave(id, studyId)));

        var outcomes = runTogether(tasks);

        assertThat(outcomes.getFirst().success()).isTrue();
        for (int i = 0; i < participants.size(); i++) {
            var outcome = outcomes.get(i + 1);
            assertThat(jdbcTemplate.queryForList("select reason from study_history where member_id = ?",
                    String.class, participants.get(i)))
                    .containsExactly(outcome.success() ? "LEFT" : "STUDY_DELETED");
            assertThat(outcome.error()).isIn(null, ErrorCode.STUDY_NOT_FOUND);
        }
        assertThat(count("study_member")).isZero();
    }

    @RepeatedTest(10)
    @DisplayName("삭제·참여·중복 참여·탈퇴·상태 변경·다른 스터디 삭제가 섞여도 데드락이 없다")
    void mixedOperations() throws Exception {
        Long ownerA = member("ownerA");
        Long ownerB = member("ownerB");
        Long a = open(ownerA, 20, DayOfWeek.MONDAY);
        Long b = open(ownerB, 20, DayOfWeek.TUESDAY);
        List<Callable<?>> tasks = new ArrayList<>();
        tasks.add(() -> delete(ownerA, a));
        tasks.add(() -> delete(ownerB, b));
        tasks.add(() -> studyService.changeRecruitStatus(ownerA, a,
                new RecruitStatusRequest(RecruitStatus.CLOSED)));
        for (int i = 0; i < 4; i++) {
            // 두 스터디에 함께 참여 중인 회원들이 두 삭제의 잠금 대상이 겹치게 한다.
            Long both = member("both" + i);
            participationService.join(both, a);
            participationService.join(both, b);
            Long applicant = member("new" + i);
            tasks.add(() -> leave(both, a));
            tasks.add(() -> participationService.join(both, b));
            tasks.add(() -> participationService.join(applicant, a));
            tasks.add(() -> participationService.join(applicant, b));
        }

        // 업무 오류가 아닌 예외(데드락, 잠금 시간 초과 등)가 나면 runTogether에서 실패한다.
        var outcomes = runTogether(tasks);

        assertThat(outcomes.get(0).success()).isTrue();
        assertThat(outcomes.get(1).success()).isTrue();
        assertThat(count("study")).isZero();
        assertThat(count("study_member")).isZero();
    }

    @Test
    @DisplayName("스터디 잠금을 기다린 참여 신청은 그 사이에 커밋된 인원을 보고 거절한다")
    void joinSeesLatestMemberCount() throws Exception {
        Long studyId = open(member("owner"), 2, DayOfWeek.MONDAY);
        Long applicant = member("applicant");
        Long other = member("other");

        Throwable failure = whileHolding(
                () -> studyRepository.findByIdForUpdate(studyId).orElseThrow(),
                () -> participationService.join(applicant, studyId),
                () -> insertStudyMember(studyId, other));

        assertErrorCode(failure, ErrorCode.STUDY_FULL);
    }

    @Test
    @DisplayName("회원 잠금을 기다린 참여 신청은 그 사이에 커밋된 참여 정보와의 겹침을 찾는다")
    void joinSeesLatestSchedule() throws Exception {
        Long first = open(member("ownerA"), 10, DayOfWeek.MONDAY);
        Long second = open(member("ownerB"), 10, DayOfWeek.MONDAY);
        Long member = member("member");

        Throwable failure = whileHolding(
                () -> jdbcTemplate.queryForList("select id from member where id = ? for update", member),
                () -> participationService.join(member, second),
                () -> insertStudyMember(first, member));

        assertErrorCode(failure, ErrorCode.TIME_OVERLAP);
    }

    /**
     * 잠금을 잡은 트랜잭션이 있는 동안 작업을 시작해 대기시킨다.
     * 대기 중에 데이터를 바꾸고 커밋한 뒤, 작업이 실패한 원인을 반환한다.
     */
    private Throwable whileHolding(Runnable lock, Runnable task, Runnable change) throws Exception {
        try (ExecutorService executor = Executors.newSingleThreadExecutor()) {
            Future<?> waiting = new TransactionTemplate(transactionManager).execute(status -> {
                lock.run();
                Future<?> future = executor.submit(task);
                assertThatThrownBy(() -> future.get(1, TimeUnit.SECONDS)).isInstanceOf(TimeoutException.class);
                change.run();
                return future;
            });
            try {
                waiting.get(10, TimeUnit.SECONDS);
            } catch (ExecutionException e) {
                return e.getCause();
            }
            return null;
        }
    }

    private void assertErrorCode(Throwable failure, ErrorCode expected) {
        assertThat(failure).isInstanceOfSatisfying(BusinessException.class,
                e -> assertThat(e.errorCode()).isEqualTo(expected));
    }

    private void insertStudyMember(Long studyId, Long memberId) {
        jdbcTemplate.update("insert into study_member (study_id, member_id) values (?, ?)", studyId, memberId);
    }

    private Void delete(Long ownerId, Long studyId) {
        deletionService.delete(ownerId, studyId);
        return null;
    }

    private Void leave(Long memberId, Long studyId) {
        participationService.leave(memberId, studyId);
        return null;
    }

    private Long member(String name) {
        return memberService.signUp(new SignupRequest(name + "@example.com", "password123", name, "")).id();
    }

    private Long open(Long ownerId, int capacity, DayOfWeek day) {
        return studyService.open(ownerId, request(capacity, day)).id();
    }

    private CreateStudyRequest request(int capacity, DayOfWeek day) {
        return new CreateStudyRequest("스터디", "내용", capacity, RecruitStatus.RECRUITING,
                day, LocalTime.of(19, 0), LocalTime.of(21, 0));
    }

    /** 인자는 테이블 이름과 선택적인 where 절이다. */
    private long count(String from) {
        return jdbcTemplate.queryForObject("select count(*) from " + from, Long.class);
    }
}
