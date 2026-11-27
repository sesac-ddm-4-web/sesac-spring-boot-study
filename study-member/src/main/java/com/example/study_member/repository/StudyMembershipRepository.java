package com.example.study_member.repository;

import com.example.study_member.domain.StudyMembership;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudyMembershipRepository extends JpaRepository<StudyMembership, Long> {
    List<StudyMembership> findAllByMemberId(Long memberId);

    Optional<StudyMembership> findByStudyIdAndMemberId(Long studyId, Long memberId);

    long countByStudyId(Long studyId);
}
