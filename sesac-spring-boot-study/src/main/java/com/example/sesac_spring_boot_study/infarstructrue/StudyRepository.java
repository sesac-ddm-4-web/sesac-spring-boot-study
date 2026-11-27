package com.example.sesac_spring_boot_study.infarstructrue;

import com.example.sesac_spring_boot_study.domain.Study;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StudyRepository extends JpaRepository<Study, Long> {
    @EntityGraph(attributePaths = "leader")
    List<Study> findAllByOrderByIdDesc();

    @EntityGraph(attributePaths = "leader")
    Optional<Study> findWithLeaderById(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Study s where s.id = :id")
    Optional<Study> findByIdForUpdate(@Param("id") Long id);

}
