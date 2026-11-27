package com.example.sesac_spring_boot_study.repository;

import com.example.sesac_spring_boot_study.domain.StudyGroup;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudyGroupRepository extends JpaRepository<StudyGroup, Long> {
    List<StudyGroup> findAllByOrderByIdAsc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from StudyGroup s where s.id = :id")
    Optional<StudyGroup> findForUpdate(@Param("id") Long id);
}
