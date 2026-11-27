package com.example.studyservice.schedule.domain;

import com.example.studyservice.global.exception.ApiException;
import com.example.studyservice.global.exception.ErrorCode;
import com.example.studyservice.study.domain.Study;
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

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "schedule", indexes = @Index(name = "idx_schedule_study_start", columnList = "study_id, startAt"))
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "study_id", nullable = false)
    private Study study;

    @Column(nullable = false, length = 50)
    private String title;

    @Column(length = 500)
    private String description;

    @Column(length = 100)
    private String location;

    @Column(nullable = false)
    private LocalDateTime startAt;

    @Column(nullable = false)
    private LocalDateTime endAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    protected Schedule() {
    }

    public Schedule(Study study, String title, String description, String location,
                    LocalDateTime startAt, LocalDateTime endAt, LocalDateTime createdAt) {
        this.study = Objects.requireNonNull(study);
        this.createdAt = Objects.requireNonNull(createdAt);
        change(title, description, location, startAt, endAt);
    }

    public void update(String title, String description, String location,
                       LocalDateTime startAt, LocalDateTime endAt) {
        change(title, description, location, startAt, endAt);
    }

    private void change(String title, String description, String location,
                        LocalDateTime startAt, LocalDateTime endAt) {
        if (!endAt.isAfter(startAt)) {
            throw new ApiException(ErrorCode.INVALID_SCHEDULE_TIME);
        }
        this.title = Objects.requireNonNull(title).strip();
        this.description = blankToNull(description);
        this.location = blankToNull(location);
        this.startAt = startAt;
        this.endAt = endAt;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.strip();
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

    public String getDescription() {
        return description;
    }

    public String getLocation() {
        return location;
    }

    public LocalDateTime getStartAt() {
        return startAt;
    }

    public LocalDateTime getEndAt() {
        return endAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
