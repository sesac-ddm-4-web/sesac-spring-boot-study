package com.example.personal_project.study.domain;

import com.example.personal_project.common.DomainText;
import com.example.personal_project.common.InvalidDomainFieldException;
import jakarta.persistence.CheckConstraint;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Entity
@Table(
        name = "studies",
        check = {
                @CheckConstraint(name = "ck_studies_capacity", constraint = "capacity between 2 and 100"),
                @CheckConstraint(name = "ck_studies_recruitment_status", constraint = "cast(recruitment_status as binary) in ('OPEN', 'CLOSED')")
        }
)
public class Study {

    private static final int TITLE_MAX_LENGTH = 100;
    private static final int DESCRIPTION_MAX_LENGTH = 2000;
    private static final int MIN_CAPACITY = 2;
    private static final int MAX_CAPACITY = 100;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = TITLE_MAX_LENGTH)
    private String title;

    @Column(nullable = false, length = DESCRIPTION_MAX_LENGTH)
    private String description;

    @Column(nullable = false)
    private int capacity;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "recruitment_status", nullable = false, length = 10)
    private RecruitmentStatus recruitmentStatus;

    @Column(name = "created_at", nullable = false, updatable = false, secondPrecision = 6)
    private Instant createdAt;

    @OneToMany(mappedBy = "study")
    private List<StudyMeeting> meetings = new ArrayList<>();

    protected Study() {
    }

    private Study(String title, String description, int capacity, Instant now) {
        if (capacity < MIN_CAPACITY || capacity > MAX_CAPACITY) {
            throw new InvalidDomainFieldException("capacity", "capacity must be between " + MIN_CAPACITY + " and " + MAX_CAPACITY);
        }
        this.title = DomainText.requireLength(title, "title", 1, TITLE_MAX_LENGTH);
        this.description = DomainText.requireLength(description, "description", 1, DESCRIPTION_MAX_LENGTH);
        this.capacity = capacity;
        this.recruitmentStatus = RecruitmentStatus.OPEN;
        this.createdAt = Objects.requireNonNull(now, "now").truncatedTo(ChronoUnit.MICROS);
    }

    public static Study open(String title, String description, int capacity, Instant now) {
        return new Study(title, description, capacity, now);
    }

    public void changeRecruitmentStatus(RecruitmentStatus status) {
        recruitmentStatus = Objects.requireNonNull(status, "status");
    }

    public boolean isRecruiting() {
        return recruitmentStatus == RecruitmentStatus.OPEN;
    }

    public StudyMeeting addMeeting(String title, String location, Instant startsAt, Instant endsAt, Instant now) {
        StudyMeeting meeting = new StudyMeeting(this, title, location, startsAt, endsAt, now);
        meetings.add(meeting);
        return meeting;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getCapacity() {
        return capacity;
    }

    public RecruitmentStatus getRecruitmentStatus() {
        return recruitmentStatus;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public List<StudyMeeting> getMeetings() {
        return Collections.unmodifiableList(meetings);
    }
}
