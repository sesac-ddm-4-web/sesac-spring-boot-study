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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Study extends BaseEntity {

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private int capacity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leader_id", nullable = false)
    private Member leader;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StudyStatus status;

    private Study(String name, String description, int capacity, Member leader) {
        this.name = name;
        this.description = description;
        this.capacity = capacity;
        this.leader = leader;
        this.status = StudyStatus.RECRUITING;
    }

    public static Study create(String name, String description, int capacity, Member leader) {
        return new Study(name, description, capacity, leader);
    }

    public boolean isLeader(Long memberId) {
        return leader.getId().equals(memberId);
    }

    public boolean isRecruiting() {
        return status == StudyStatus.RECRUITING;
    }

    public void close() {
        this.status = StudyStatus.CLOSED;
    }
}
