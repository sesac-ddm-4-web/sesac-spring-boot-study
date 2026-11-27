package com.sesac.studyhub.study.repository;

import com.sesac.studyhub.study.domain.StudyMember;
import com.sesac.studyhub.study.dto.StudyMemberCount;
import java.util.Optional;
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

    boolean existsByStudyIdAndMemberId(Long studyId, Long memberId);

    Optional<StudyMember> findByStudyIdAndMemberId(Long studyId, Long memberId);

    // 참여자 목록: 회원·프로필까지 한 번에, 스터디장 먼저 → 가입순
    @Query("""
        select sm from StudyMember sm
        join fetch sm.member m
        join fetch m.profile
        where sm.study.id = :studyId
        order by sm.role asc, sm.createdAt asc
        """)
    List<StudyMember> findAllWithMemberByStudyId(@Param("studyId") Long studyId);

    // 내 스터디 목록: 스터디 정보까지 한 번에, 최근 참여순
    @Query("""
        select sm from StudyMember sm
        join fetch sm.study
        where sm.member.id = :memberId
        order by sm.createdAt desc
        """)
    List<StudyMember> findAllWithStudyByMemberId(@Param("memberId") Long memberId);
}