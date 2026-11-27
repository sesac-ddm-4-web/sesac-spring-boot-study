package com.sesac.studyhub.study.repository;

import com.sesac.studyhub.study.domain.StudyMember;
import com.sesac.studyhub.study.dto.StudyMemberCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface StudyMemberRepository extends JpaRepository<StudyMember, Long> {

    long countByStudyId(Long studyId);

    // 목록의 여러 스터디 인원을 쿼리 한 번으로 집계
    @Query("""
            select new com.sesac.studyhub.study.dto.StudyMemberCount(sm.study.id, count(sm))
            from StudyMember sm
            where sm.study.id in :studyIds
            group by sm.study.id
            """)
    List<StudyMemberCount> countByStudyIds(@Param("studyIds") Collection<Long> studyIds);
}