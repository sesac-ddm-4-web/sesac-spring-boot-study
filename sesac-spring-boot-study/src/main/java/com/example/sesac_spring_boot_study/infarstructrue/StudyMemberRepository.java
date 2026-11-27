package com.example.sesac_spring_boot_study.infarstructrue;

import com.example.sesac_spring_boot_study.domain.StudyMember;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudyMemberRepository extends JpaRepository<StudyMember, Long> {
    boolean existsByMemberIdAndStudyId(Long memberId, Long studyId);

    long countByStudyId(Long studyId);

    Optional<StudyMember> findByMemberIdAndStudyId(Long memberId, Long studyId);

    @EntityGraph(attributePaths = "study")
    List<StudyMember> findByMemberIdOrderByJoinedAtDesc(Long memberId);

    @EntityGraph(attributePaths = "member")
    List<StudyMember> findByStudyIdOrderByJoinedAtAsc(Long studyId);
}
