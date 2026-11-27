package com.example.studyservice.study.domain;

import com.example.studyservice.member.domain.Member;
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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "study_member",
        uniqueConstraints = @UniqueConstraint(name = "uk_study_member", columnNames = {"study_id", "member_id"}))
public class StudyMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "study_id", nullable = false)
    private Study study;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StudyRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ParticipationStatus status;

    @Column(nullable = false)
    private LocalDateTime joinedAt;

    private LocalDateTime leftAt;

    protected StudyMember() {
    }

    private StudyMember(Study study, Member member, StudyRole role, LocalDateTime joinedAt) {
        this.study = Objects.requireNonNull(study);
        this.member = Objects.requireNonNull(member);
        this.role = role;
        this.status = ParticipationStatus.ACTIVE;
        this.joinedAt = Objects.requireNonNull(joinedAt);
    }

    public static StudyMember leader(Study study, Member member, LocalDateTime now) {
        return new StudyMember(study, member, StudyRole.LEADER, now);
    }

    public static StudyMember member(Study study, Member member, LocalDateTime now) {
        return new StudyMember(study, member, StudyRole.MEMBER, now);
    }

    public void rejoin(LocalDateTime now) {
        if (isActive()) {
            throw new IllegalStateException("이미 참여 중인 회원입니다.");
        }
        this.role = StudyRole.MEMBER;
        this.status = ParticipationStatus.ACTIVE;
        this.joinedAt = now;
        this.leftAt = null;
    }

    public void leave(LocalDateTime now) {
        this.status = ParticipationStatus.LEFT;
        this.leftAt = now;
    }

    public void handOverLeaderTo(StudyMember next) {
        if (!isLeader() || !next.isActive()) {
            throw new IllegalStateException("스터디장만 참여 중인 멤버에게 위임할 수 있습니다.");
        }
        this.role = StudyRole.MEMBER;
        next.role = StudyRole.LEADER;
    }

    public boolean isActive() {
        return status == ParticipationStatus.ACTIVE;
    }

    public boolean isLeader() {
        return role == StudyRole.LEADER;
    }

    public Long getId() {
        return id;
    }

    public Study getStudy() {
        return study;
    }

    public Member getMember() {
        return member;
    }

    public StudyRole getRole() {
        return role;
    }

    public ParticipationStatus getStatus() {
        return status;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public LocalDateTime getLeftAt() {
        return leftAt;
    }
}
