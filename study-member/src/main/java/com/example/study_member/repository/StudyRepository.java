package com.example.study_member.repository;

import com.example.study_member.domain.Study;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface StudyRepository extends JpaRepository<Study, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Study> findLockedById(Long studyId);
}
