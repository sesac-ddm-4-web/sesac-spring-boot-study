package com.example.study_member.Domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
public class StudyMeeting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "study_id", nullable = false)
    private Study study;

    @Column(nullable = false)
    private String title;

    @ManyToOne(optional = false)
    @JoinColumn(name = "creator_id", nullable = false)
    private Member creator;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime startsAt;

    @Column(nullable = false)
    private LocalDateTime finishesAt;

    @Column(nullable = false)
    private String location;

    private String description;

    protected StudyMeeting() {
    }

    public StudyMeeting(StudyMembership membership, String title, LocalDateTime startsAt,
                        LocalDateTime finishesAt, String location, String description) {
        Objects.requireNonNull(membership, "membership");
        this.startsAt = Objects.requireNonNull(startsAt, "startsAt");
        this.finishesAt = Objects.requireNonNull(finishesAt, "finishesAt");
        if (!finishesAt.isAfter(startsAt)) {
            throw new IllegalArgumentException("종료 시각은 시작 시각보다 늦어야 합니다.");
        }
        this.study = Objects.requireNonNull(membership.getStudy(), "membership.study");
        this.creator = Objects.requireNonNull(membership.getMember(), "membership.member");
        this.title = Objects.requireNonNull(title, "title");
        this.location = Objects.requireNonNull(location, "location");
        this.description = description;
        this.createdAt = LocalDateTime.now();
    }

    public Member getCreator() {
        return creator;
    }

    public Study getStudy() {
        return study;
    }
}
