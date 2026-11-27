package com.example.personal_project.study.domain;

import com.example.personal_project.member.domain.Member;
import jakarta.persistence.CheckConstraint;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

@Entity
@Table(
        name = "study_memberships",
        uniqueConstraints = @UniqueConstraint(name = "uk_study_memberships_study_member", columnNames = {"study_id", "member_id"}),
        indexes = {
                @Index(name = "ix_study_memberships_study_status", columnList = "study_id, status, joined_at, id"),
                @Index(name = "ix_study_memberships_member_status", columnList = "member_id, status, joined_at, id")
        },
        check = {
                @CheckConstraint(name = "ck_study_memberships_role", constraint = "cast(role as binary) in ('OWNER', 'PARTICIPANT')"),
                @CheckConstraint(name = "ck_study_memberships_status", constraint = "cast(status as binary) in ('ACTIVE', 'LEFT')"),
                @CheckConstraint(name = "ck_study_memberships_owner_active", constraint = "role <> 'OWNER' or status = 'ACTIVE'"),
                @CheckConstraint(name = "ck_study_memberships_left_at", constraint = """
                        (status = 'ACTIVE' and left_at is null)
                        or (status = 'LEFT' and left_at is not null and left_at >= joined_at)
                        """)
        }
)
public class StudyMembership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "study_id", nullable = false, updatable = false)
    private Study study;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false, updatable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private MembershipRole role;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 10)
    private MembershipStatus status;

    @Column(name = "joined_at", nullable = false, secondPrecision = 6)
    private Instant joinedAt;

    @Column(name = "left_at", secondPrecision = 6)
    private Instant leftAt;

    protected StudyMembership() {
    }

    private StudyMembership(Study study, Member member, MembershipRole role, Instant now) {
        this.study = Objects.requireNonNull(study, "study");
        this.member = Objects.requireNonNull(member, "member");
        this.role = role;
        this.status = MembershipStatus.ACTIVE;
        this.joinedAt = Objects.requireNonNull(now, "now").truncatedTo(ChronoUnit.MICROS);
    }

    public static StudyMembership owner(Study study, Member member, Instant now) {
        return new StudyMembership(study, member, MembershipRole.OWNER, now);
    }

    public static StudyMembership join(Study study, Member member, Instant now) {
        if (!study.isRecruiting()) {
            throw new IllegalStateException("recruitment is closed");
        }
        return new StudyMembership(study, member, MembershipRole.PARTICIPANT, now);
    }

    public void leave(Instant now) {
        if (role == MembershipRole.OWNER) {
            throw new IllegalStateException("a study owner cannot leave");
        }
        if (status == MembershipStatus.LEFT) {
            return;
        }
        Instant departureTime = Objects.requireNonNull(now, "now").truncatedTo(ChronoUnit.MICROS);
        if (departureTime.isBefore(joinedAt)) {
            throw new IllegalArgumentException("leftAt cannot be before joinedAt");
        }
        status = MembershipStatus.LEFT;
        leftAt = departureTime;
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

    public MembershipRole getRole() {
        return role;
    }

    public MembershipStatus getStatus() {
        return status;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }

    public Instant getLeftAt() {
        return leftAt;
    }
}
