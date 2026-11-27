package com.sesac.studyhub.study.domain;

import com.sesac.studyhub.global.common.BaseEntity;
import com.sesac.studyhub.member.domain.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
    name = "study_member",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_study_member",
        columnNames = {"study_id", "member_id"})
)
public class StudyMember extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_id", nullable = false)
    private Study study;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StudyRole role;

    private StudyMember(Study study, Member member, StudyRole role) {
        this.study = study;
        this.member = member;
        this.role = role;
    }

    public static StudyMember leader(Study study, Member member) {
        return new StudyMember(study, member, StudyRole.LEADER);
    }

    public static StudyMember member(Study study, Member member) {
        return new StudyMember(study, member, StudyRole.MEMBER);
    }

    public boolean isLeader() {
        return role == StudyRole.LEADER;
    }
}
