package com.example.personal_project.study.domain;

import com.example.personal_project.common.DomainText;
import com.example.personal_project.common.InvalidDomainFieldException;
import jakarta.persistence.CheckConstraint;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

@Entity
@Table(
        name = "study_meetings",
        indexes = @Index(name = "ix_study_meetings_study_starts_at", columnList = "study_id, starts_at, id"),
        check = @CheckConstraint(name = "ck_study_meetings_time", constraint = "ends_at > starts_at")
)
public class StudyMeeting {

    private static final int TITLE_MAX_LENGTH = 100;
    private static final int LOCATION_MAX_LENGTH = 255;
    private static final Instant EARLIEST_DATETIME = Instant.parse("1000-01-01T00:00:00Z");
    private static final Instant LATEST_DATETIME = Instant.parse("9999-12-31T23:59:59.499999Z");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "study_id", nullable = false, updatable = false)
    private Study study;

    @Column(nullable = false, length = TITLE_MAX_LENGTH)
    private String title;

    @Column(nullable = false, length = LOCATION_MAX_LENGTH)
    private String location;

    @Column(name = "starts_at", nullable = false, secondPrecision = 6)
    private Instant startsAt;

    @Column(name = "ends_at", nullable = false, secondPrecision = 6)
    private Instant endsAt;

    @Column(name = "created_at", nullable = false, updatable = false, secondPrecision = 6)
    private Instant createdAt;

    protected StudyMeeting() {
    }

    StudyMeeting(Study study, String title, String location, Instant startsAt, Instant endsAt, Instant now) {
        this.startsAt = requireMeetingTime(startsAt, "startsAt");
        this.endsAt = requireMeetingTime(endsAt, "endsAt");
        this.createdAt = Objects.requireNonNull(now, "now").truncatedTo(ChronoUnit.MICROS);
        if (!this.startsAt.isAfter(now)) {
            throw new InvalidDomainFieldException("startsAt", "startsAt must be in the future");
        }
        if (!this.endsAt.isAfter(this.startsAt)) {
            throw new InvalidDomainFieldException("endsAt", "endsAt must be after startsAt");
        }
        this.study = Objects.requireNonNull(study, "study");
        this.title = DomainText.requireLength(title, "title", 1, TITLE_MAX_LENGTH);
        this.location = DomainText.requireLength(location, "location", 1, LOCATION_MAX_LENGTH);
    }

    private static Instant requireMeetingTime(Instant time, String field) {
        if (time == null) {
            throw new InvalidDomainFieldException(field, field + " is required");
        }
        if (time.getNano() % 1_000 != 0) {
            throw new InvalidDomainFieldException(field, field + " must have microsecond precision or coarser");
        }
        if (time.isBefore(EARLIEST_DATETIME) || time.isAfter(LATEST_DATETIME)) {
            throw new InvalidDomainFieldException(field, field + " is outside the MySQL DATETIME range");
        }
        return time;
    }

    public Long getId() {
        return id;
    }

    public Study getStudy() {
        return study;
    }

    public String getTitle() {
        return title;
    }

    public String getLocation() {
        return location;
    }

    public Instant getStartsAt() {
        return startsAt;
    }

    public Instant getEndsAt() {
        return endsAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
