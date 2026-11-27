package com.example.study.domain;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** 탈퇴·삭제 이력. 스터디가 삭제돼도 남도록 스터디 정보는 값으로 복사한다. */
@Entity
@Table(name = "study_history")
public class StudyHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private Long studyId;

    @Column(nullable = false, length = Study.NAME_MAX_LENGTH)
    private String studyName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HistoryReason reason;

    @Column(nullable = false)
    private LocalDateTime endedAt;

    protected StudyHistory() {
    }

    StudyHistory(Member member, Study study, HistoryReason reason, LocalDateTime endedAt) {
        this.member = member;
        this.studyId = study.getId();
        this.studyName = study.getName();
        this.reason = reason;
        this.endedAt = endedAt.truncatedTo(ChronoUnit.SECONDS);
    }

    public Long getId() {
        return id;
    }

    public Member getMember() {
        return member;
    }

    public Long getStudyId() {
        return studyId;
    }

    public String getStudyName() {
        return studyName;
    }

    public HistoryReason getReason() {
        return reason;
    }

    public LocalDateTime getEndedAt() {
        return endedAt;
    }
}
