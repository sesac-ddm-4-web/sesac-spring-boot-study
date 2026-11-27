package com.example.study.repository;

import java.util.List;

import com.example.study.domain.StudyMember;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudyMemberRepository extends JpaRepository<StudyMember, Long> {

    /** 잠금 순서를 고정하기 위해 회원 ID 오름차순으로 반환한다. */
    @Query("select sm.member.id from StudyMember sm where sm.study.id = :studyId order by sm.member.id")
    List<Long> findMemberIds(@Param("studyId") Long studyId);
}
