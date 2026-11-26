package com.example.study_member.Domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"study_id", "member_id"}))
public class StudyMembership {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "study_id", nullable = false)
    private Study study;

    @ManyToOne(optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private LocalDateTime joinedAt;

    protected StudyMembership() {
    }

    public StudyMembership(Study study, Member member) {
        this.study = Objects.requireNonNull(study, "study");
        this.member = Objects.requireNonNull(member, "member");
        this.joinedAt = LocalDateTime.now();
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = Objects.requireNonNull(member, "member");
    }

    public Study getStudy() {
        return study;
    }

    public void setStudy(Study study) {
        this.study = Objects.requireNonNull(study, "study");
    }
}
