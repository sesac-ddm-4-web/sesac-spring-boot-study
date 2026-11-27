package com.example.sesac_spring_boot_study.presentation;

import com.example.sesac_spring_boot_study.domain.Study;
import com.example.sesac_spring_boot_study.domain.StudyMember;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

public class StudyDto {
    private StudyDto() {
    }

    public record CreateRequest(
            @NotNull Long leaderId,
            @NotBlank @Size(max = 100) String title,
            @Size(max = 1000) String description,
            @Min(2) int maxCapacity
    ) {
    }

    public record JoinRequest(@NotNull Long memberId) {
    }

    public record Summary(Long id, String title, String leaderNickname, int maxCapacity) {
        public static Summary from(Study study) {
            return new Summary(study.getId(), study.getTitle(),
                    study.getLeader().getNickname(), study.getMaxCapacity());
        }
    }

    public record Detail(Long id, String title, String description,
                         Long leaderId, String leaderNickname,
                         int maxCapacity, int currentMemberCount,
                         LocalDateTime createdAt, List<Participant> members) {
        public static Detail of(Study study, List<StudyMember> studyMembers) {
            List<Participant> participants = studyMembers.stream()
                    .map(Participant::from)
                    .toList();
            return new Detail(study.getId(), study.getTitle(), study.getDescription(),
                    study.getLeader().getId(), study.getLeader().getNickname(),
                    study.getMaxCapacity(), participants.size(),
                    study.getCreatedAt(), participants);
        }
    }

    public record Participant(Long memberId, String nickname, LocalDateTime joinedAt) {
        public static Participant from(StudyMember sm) {
            return new Participant(sm.getMember().getId(),
                    sm.getMember().getNickname(), sm.getJoinedAt());
        }
    }

    public record JoinedStudy(Long studyId, String title, boolean leader, LocalDateTime joinedAt) {
        public static JoinedStudy from(StudyMember sm) {
            Study study = sm.getStudy();
            return new JoinedStudy(study.getId(), study.getTitle(),
                    study.isLeader(sm.getMember().getId()), sm.getJoinedAt());
        }
    }
}
