package com.sesac.studyhub.study.repository;

import com.sesac.studyhub.study.domain.Study;
import com.sesac.studyhub.study.domain.StudyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StudyRepository extends JpaRepository<Study, Long> {

    // 목록: 스터디장 정보까지 한 번에, status가 null이면 전체
    @Query(value = """
            select s from Study s join fetch s.leader
            where (:status is null or s.status = :status)
            """,
        countQuery = """
            select count(s) from Study s
            where (:status is null or s.status = :status)
            """)
    Page<Study> findAllWithLeader(@Param("status") StudyStatus status, Pageable pageable);

    @Query("select s from Study s join fetch s.leader where s.id = :id")
    Optional<Study> findWithLeaderById(@Param("id") Long id);

}
