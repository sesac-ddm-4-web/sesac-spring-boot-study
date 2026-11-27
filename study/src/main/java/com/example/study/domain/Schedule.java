package com.example.study.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** 모임 일정. 일시는 한국 시간이다. */
@Entity
@Table(name = "schedule")
public class Schedule {

    public static final int PLACE_MAX_LENGTH = 100;
    public static final int CONTENT_MAX_LENGTH = 1000;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "study_id", nullable = false)
    private Study study;

    @Column(nullable = false)
    private LocalDateTime meetingAt;

    @Column(nullable = false, length = PLACE_MAX_LENGTH)
    private String place;

    @Column(nullable = false, length = CONTENT_MAX_LENGTH)
    private String content;

    protected Schedule() {
    }

    /** {@link Study}를 통해서만 만든다. */
    Schedule(Study study, LocalDateTime meetingAt, String place, String content) {
        this.study = study;
        this.meetingAt = meetingAt;
        this.place = place;
        this.content = content;
    }

    public Long getId() {
        return id;
    }

    public Study getStudy() {
        return study;
    }

    public LocalDateTime getMeetingAt() {
        return meetingAt;
    }

    public String getPlace() {
        return place;
    }

    public String getContent() {
        return content;
    }
}
