package com.example.studyservice.study.domain;

import com.example.studyservice.global.exception.ApiException;
import com.example.studyservice.global.exception.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "study")
public class Study {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private int capacity;

    @Column(nullable = false)
    private int memberCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StudyStatus status = StudyStatus.ACTIVE;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime closedAt;

    protected Study() {
    }

    public Study(String title, String description, int capacity, LocalDateTime createdAt) {
        this.title = Objects.requireNonNull(title).strip();
        this.description = blankToNull(description);
        this.capacity = capacity;
        this.memberCount = 1;
        this.createdAt = Objects.requireNonNull(createdAt);
    }

    public void update(String title, String description, int capacity) {
        validateActive();
        if (capacity < memberCount) {
            throw new ApiException(ErrorCode.CAPACITY_BELOW_MEMBER_COUNT, memberCount);
        }
        this.title = Objects.requireNonNull(title).strip();
        this.description = blankToNull(description);
        this.capacity = capacity;
    }

    public void validateJoinable() {
        validateActive();
        if (isFull()) {
            throw new ApiException(ErrorCode.STUDY_FULL, capacity);
        }
    }

    public void validateActive() {
        if (status == StudyStatus.CLOSED) {
            throw new ApiException(ErrorCode.STUDY_CLOSED);
        }
    }

    public void increaseMemberCount() {
        validateJoinable();
        memberCount++;
    }

    public void decreaseMemberCount() {
        if (memberCount <= 0) {
            throw new IllegalStateException("참여 인원이 0명인 스터디에서 탈퇴할 수 없습니다.");
        }
        memberCount--;
    }

    public void close(LocalDateTime now) {
        this.status = StudyStatus.CLOSED;
        this.closedAt = now;
    }

    public boolean isFull() {
        return memberCount >= capacity;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getMemberCount() {
        return memberCount;
    }

    public StudyStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getClosedAt() {
        return closedAt;
    }
}
