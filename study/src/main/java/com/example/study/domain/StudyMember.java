package com.example.study.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/** 회원의 스터디 가입 정보. 스터디장도 포함한다. */
@Entity
@Table(name = "study_member", uniqueConstraints =
        @UniqueConstraint(name = "uk_study_member", columnNames = { "study_id", "member_id" }))
public class StudyMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "study_id", nullable = false)
    private Study study;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    protected StudyMember() {
    }

    /** {@link Study}를 통해서만 만든다. */
    StudyMember(Study study, Member member) {
        this.study = study;
        this.member = member;
    }

    void detach() {
        this.study = null;
    }

    boolean isMember(Long memberId) {
        return member.getId().equals(memberId);
    }

    public Long getId() {
        return id;
    }

    public Study getStudy() {
        return study;
    }

    public Member getMember() {
        return member;
    }
}
