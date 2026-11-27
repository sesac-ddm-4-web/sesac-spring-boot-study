package com.example.sesac_spring_boot_study.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "study_members", uniqueConstraints =
@UniqueConstraint(columnNames = {"member_id", "study_id"}))
public class StudyMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "study_id", nullable = false)
    private Study study;

    @Column(nullable = false)
    private LocalDateTime joinedAt;

    protected StudyMember() {
    }

    public StudyMember(Member member, Study study, LocalDateTime joinedAt) {
        this.member = member;
        this.study = study;
        this.joinedAt = joinedAt;
    }

    public Long getId() { return id; }
    public Member getMember() { return member; }
    public Study getStudy() { return study; }
    public LocalDateTime getJoinedAt() { return joinedAt; }
}
