package com.sesac.studyhub.schedule.domain;

import com.sesac.studyhub.global.common.BaseEntity;
import com.sesac.studyhub.study.domain.Study;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Schedule extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_id", nullable = false)
    private Study study;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(length = 100)
    private String place;

    @Column(nullable = false)
    private LocalDateTime startAt;

    @Column(nullable = false)
    private LocalDateTime endAt;

    private Schedule(Study study, String title, String place,
        LocalDateTime startAt, LocalDateTime endAt) {
        this.study = study;
        this.title = title;
        this.place = place;
        this.startAt = startAt;
        this.endAt = endAt;
    }

    public static Schedule create(Study study, String title, String place,
        LocalDateTime startAt, LocalDateTime endAt) {
        return new Schedule(study, title, place, startAt, endAt);
    }
}
