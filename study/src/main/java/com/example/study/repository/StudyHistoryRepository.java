package com.example.study.repository;

import java.util.List;

import com.example.study.domain.StudyHistory;

import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyHistoryRepository extends JpaRepository<StudyHistory, Long> {

    /** 최신순. 같은 초에 생긴 이력은 나중에 저장한 것이 앞이다. */
    List<StudyHistory> findByMemberIdOrderByEndedAtDescIdDesc(Long memberId);
}
