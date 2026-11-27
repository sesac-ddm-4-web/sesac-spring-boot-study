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
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "study_participations", uniqueConstraints =
        @UniqueConstraint(columnNames = {"study_id", "member_id"}))
public class Participation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "study_id", nullable = false)
    private StudyGroup study;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;
    @Column(nullable = false)
    private Instant joinedAt;

    protected Participation() {}

    public Participation(StudyGroup study, Member member) {
        this.study = study;
        this.member = member;
        this.joinedAt = Instant.now();
    }

    public Member getMember() {
        return member;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }
}
