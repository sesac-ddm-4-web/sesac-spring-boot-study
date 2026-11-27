package com.example.personal_project.study.application;

import com.example.personal_project.study.domain.RecruitmentStatus;
import com.example.personal_project.study.domain.Study;

import java.time.Instant;

public final class StudyView {

    private StudyView() {
    }

    public record Summary(
            long studyId, String title, long ownerMemberId, int capacity, long activeMemberCount,
            long availableSlots, RecruitmentStatus recruitmentStatus, Instant createdAt) {

        static Summary from(Study study, long ownerMemberId, long activeMemberCount) {
            return new Summary(study.getId(), study.getTitle(), ownerMemberId, study.getCapacity(), activeMemberCount,
                    study.getCapacity() - activeMemberCount, study.getRecruitmentStatus(), study.getCreatedAt());
        }
    }

    public record Detail(
            long studyId, String title, String description, long ownerMemberId, int capacity, long activeMemberCount,
            long availableSlots, RecruitmentStatus recruitmentStatus, Instant createdAt) {

        static Detail from(Study study, Summary summary) {
            return new Detail(summary.studyId(), summary.title(), study.getDescription(), summary.ownerMemberId(),
                    summary.capacity(), summary.activeMemberCount(), summary.availableSlots(),
                    summary.recruitmentStatus(), summary.createdAt());
        }
    }
}
