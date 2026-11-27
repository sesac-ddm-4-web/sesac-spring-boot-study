package com.example.sesac_spring_boot_study.repository;

import com.example.sesac_spring_boot_study.domain.Participation;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParticipationRepository extends JpaRepository<Participation, Long> {
    Optional<Participation> findByStudyIdAndMemberId(Long studyId, Long memberId);
    long countByStudyId(Long studyId);

    @EntityGraph(attributePaths = "member")
    List<Participation> findByStudyIdOrderByJoinedAtAscIdAsc(Long studyId);
}
