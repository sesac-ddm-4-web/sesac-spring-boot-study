package com.example.personal_project.study.repository;

import com.example.personal_project.study.domain.RecruitmentStatus;
import com.example.personal_project.study.domain.Study;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StudyRepository extends JpaRepository<Study, Long> {

    Page<Study> findByRecruitmentStatus(RecruitmentStatus recruitmentStatus, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select study from Study study where study.id = :studyId")
    Optional<Study> findByIdForUpdate(@Param("studyId") long studyId);
}
