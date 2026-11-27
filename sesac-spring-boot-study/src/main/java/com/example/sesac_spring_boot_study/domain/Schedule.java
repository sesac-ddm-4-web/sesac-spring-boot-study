package com.example.sesac_spring_boot_study.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "schedules")
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "study_id", nullable = false)
    private Study study;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false)
    private LocalDateTime startsAt;

    protected Schedule() {
    }

    public Schedule(Study study, String title, LocalDateTime startsAt) {
        this.study = study;
        this.title = title;
        this.startsAt = startsAt;
    }

    public Long getId() { return id; }
    public Study getStudy() { return study; }
    public String getTitle() { return title; }
    public LocalDateTime getStartsAt() { return startsAt; }
}
