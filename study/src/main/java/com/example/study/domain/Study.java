package com.example.study.domain;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.example.study.common.exception.BusinessException;
import com.example.study.common.exception.ErrorCode;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

@Entity
public class Study {

    public static final int NAME_MAX_LENGTH = 50;
    public static final int DESCRIPTION_MAX_LENGTH = 2000;
    public static final int MIN_CAPACITY = 2;
    public static final int MAX_CAPACITY = 100;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private Member owner;

    @Column(nullable = false, length = NAME_MAX_LENGTH)
    private String name;

    @Column(nullable = false, length = DESCRIPTION_MAX_LENGTH)
    private String description;

    @Column(nullable = false)
    private int capacity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecruitStatus recruitStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DayOfWeek dayOfWeek;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    /** 스터디장을 포함한 가입 정보 */
    @OneToMany(mappedBy = "study", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StudyMember> members = new ArrayList<>();

    /** 스터디를 삭제하면 일정도 삭제된다. 조회는 Repository를 사용한다. */
    @OneToMany(mappedBy = "study", cascade = CascadeType.ALL)
    private List<Schedule> schedules = new ArrayList<>();

    protected Study() {
    }

    private Study(Member owner, String name, String description, int capacity, RecruitStatus recruitStatus,
            DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
        this.owner = owner;
        this.name = name;
        this.description = description;
        this.capacity = capacity;
        this.recruitStatus = recruitStatus;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    /** 스터디를 만들고 스터디장을 첫 참여자로 등록한다. */
    public static Study open(Member owner, String name, String description, int capacity,
            RecruitStatus recruitStatus, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
        if (capacity < MIN_CAPACITY || capacity > MAX_CAPACITY) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        if (!startTime.isBefore(endTime)) {
            throw new BusinessException(ErrorCode.INVALID_STUDY_TIME);
        }
        Study study = new Study(owner, name, description, capacity, recruitStatus, dayOfWeek, startTime, endTime);
        study.addMember(owner);
        return study;
    }

    /** 같은 요일에 시간이 겹치는지 확인한다. 끝과 시작이 맞닿는 것은 겹침이 아니다. */
    public boolean overlaps(Study other) {
        return dayOfWeek == other.dayOfWeek
                && startTime.isBefore(other.endTime)
                && other.startTime.isBefore(endTime);
    }

    /**
     * 참여와 재가입. 중복 참여 → 모집 상태 → 정원 → 시간 겹침 순서로 검사한다.
     *
     * @param participatingStudies 회원이 개설했거나 참여 중인 모든 스터디
     */
    public void join(Member member, List<Study> participatingStudies) {
        if (hasMember(member.getId())) {
            throw new BusinessException(ErrorCode.ALREADY_JOINED);
        }
        if (recruitStatus != RecruitStatus.RECRUITING) {
            throw new BusinessException(ErrorCode.RECRUITMENT_CLOSED);
        }
        if (members.size() >= capacity) {
            throw new BusinessException(ErrorCode.STUDY_FULL);
        }
        if (participatingStudies.stream().anyMatch(this::overlaps)) {
            throw new BusinessException(ErrorCode.TIME_OVERLAP);
        }
        // 정원이 차도 모집 상태는 바꾸지 않는다.
        addMember(member);
    }

    /** 참여 정보를 제거하고 남길 이력을 반환한다. */
    public StudyHistory leave(Long memberId, LocalDateTime endedAt) {
        if (isOwner(memberId)) {
            throw new BusinessException(ErrorCode.OWNER_CANNOT_LEAVE);
        }
        StudyMember leaving = members.stream()
                .filter(studyMember -> studyMember.isMember(memberId))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_PARTICIPANT));
        StudyHistory history = new StudyHistory(leaving.getMember(), this, HistoryReason.LEFT, endedAt);
        removeMember(leaving);
        return history;
    }

    /**
     * 모임 일정을 등록한다.
     *
     * @param now 한국 시간의 현재 시각
     */
    public Schedule addSchedule(Long requesterId, LocalDateTime meetingAt, String place, String content,
            LocalDateTime now) {
        validateOwner(requesterId);
        if (!meetingAt.isAfter(now)) {
            throw new BusinessException(ErrorCode.PAST_SCHEDULE);
        }
        // DB의 datetime이 저장할 수 있는 범위
        if (meetingAt.getYear() > 9999) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        Schedule schedule = new Schedule(this, meetingAt, place, content);
        schedules.add(schedule);
        return schedule;
    }

    /** 스터디장과 현재 참여자만 일정을 볼 수 있다. (스터디장도 참여 정보에 있다) */
    public void validateScheduleReader(Long memberId) {
        if (!isOwner(memberId) && !hasMember(memberId)) {
            throw new BusinessException(ErrorCode.SCHEDULE_ACCESS_DENIED);
        }
    }

    public void changeRecruitStatus(Long requesterId, RecruitStatus recruitStatus) {
        validateOwner(requesterId);
        this.recruitStatus = recruitStatus;
    }

    public void validateOwner(Long memberId) {
        if (!isOwner(memberId)) {
            throw new BusinessException(ErrorCode.NOT_STUDY_OWNER);
        }
    }

    public boolean isOwner(Long memberId) {
        return owner.getId().equals(memberId);
    }

    public boolean hasMember(Long memberId) {
        return members.stream().anyMatch(studyMember -> studyMember.isMember(memberId));
    }

    public StudyRole roleOf(Long memberId) {
        if (isOwner(memberId)) {
            return StudyRole.OWNER;
        }
        return hasMember(memberId) ? StudyRole.MEMBER : StudyRole.NONE;
    }

    /** 삭제 시점의 스터디장과 참여자에게 남길 이력. 이미 탈퇴한 회원은 포함되지 않는다. */
    public List<StudyHistory> deletionHistories(LocalDateTime endedAt) {
        return members.stream()
                .map(studyMember -> {
                    Member member = studyMember.getMember();
                    HistoryReason reason = isOwner(member.getId())
                            ? HistoryReason.OWNER_DELETED
                            : HistoryReason.STUDY_DELETED;
                    return new StudyHistory(member, this, reason, endedAt);
                })
                .toList();
    }

    /** 양방향 관계의 양쪽 값을 함께 맞춘다. */
    private void addMember(Member member) {
        members.add(new StudyMember(this, member));
    }

    /** 목록에서 빠진 가입 정보는 orphanRemoval로 삭제된다. */
    private void removeMember(StudyMember studyMember) {
        members.remove(studyMember);
        studyMember.detach();
    }

    public Long getId() {
        return id;
    }

    public Member getOwner() {
        return owner;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getCapacity() {
        return capacity;
    }

    public RecruitStatus getRecruitStatus() {
        return recruitStatus;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public List<StudyMember> getMembers() {
        return Collections.unmodifiableList(members);
    }
}
