package com.example.sesac_spring_boot_study.domain;

import java.time.Instant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "study_meetings")
public class Meeting {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "study_id", nullable = false)
    private StudyGroup study;
    @Column(nullable = false, length = 120)
    private String title;
    @Column(nullable = false, length = 200)
    private String location;
    @Column(nullable = false)
    private Instant startsAt;

    protected Meeting() {}

    public Meeting(StudyGroup study, String title, String location, Instant startsAt) {
        this.study = study;
        this.title = title;
        this.location = location;
        this.startsAt = startsAt;
    }

    public Long getId() {
        return id;
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
}
