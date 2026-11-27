package com.example.studyservice.study.repository;

import com.example.studyservice.study.domain.Study;
import com.example.studyservice.study.domain.StudyStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface StudyRepository extends JpaRepository<Study, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Study s where s.id = :id")
    Optional<Study> findByIdForUpdate(Long id);

    @Query("""
            select s from Study s
            where (:status is null or s.status = :status)
              and (:keyword is null or lower(s.title) like lower(concat('%', :keyword, '%')))
            order by s.id desc
            """)
    List<Study> search(StudyStatus status, String keyword);
}
