package com.example.sesac_spring_boot_study.domain;

import com.example.sesac_spring_boot_study.exception.BusinessException;
import com.example.sesac_spring_boot_study.exception.ErrorCode;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "studies")
public class Study {

    private static final int MIN_CAPACITY = 2;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private int maxCapacity;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "leader_id", nullable = false)
    private Member leader;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    protected Study() {
    }

    public Study(Member leader, String title, String description, int maxCapacity) {
        if (maxCapacity < MIN_CAPACITY) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "정원은 개설자를 포함해 2명 이상이어야 합니다.");
        }
        this.leader = leader;
        this.title = title;
        this.description = description;
        this.maxCapacity = maxCapacity;
        this.createdAt = LocalDateTime.now();
    }

    public boolean isLeader(Long memberId) {
        return leader.getId().equals(memberId);
    }

    public void validateLeader(Long memberId) {
        if (!isLeader(memberId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "스터디 개설자만 할 수 있습니다.");
        }
    }

    public void validateJoinable(long currentMemberCount) {
        if (currentMemberCount >= maxCapacity) {
            throw new BusinessException(ErrorCode.CONFLICT, "스터디 정원이 가득 찼습니다.");
        }
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public int getMaxCapacity() { return maxCapacity; }
    public Member getLeader() { return leader; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
