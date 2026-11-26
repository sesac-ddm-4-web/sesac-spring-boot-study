package com.example.study_member.Repository;

import com.example.study_member.Domain.StudyMembership;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyMembershipRepository extends JpaRepository<StudyMembership, Long> {
}
