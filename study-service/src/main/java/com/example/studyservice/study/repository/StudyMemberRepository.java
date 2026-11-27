package com.example.studyservice.study.repository;

import com.example.studyservice.study.domain.ParticipationStatus;
import com.example.studyservice.study.domain.StudyMember;
import com.example.studyservice.study.domain.StudyRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface StudyMemberRepository extends JpaRepository<StudyMember, Long> {

    @Query("""
            select sm from StudyMember sm
            where sm.study.id = :studyId and sm.member.id = :memberId
            """)
    Optional<StudyMember> findByStudyIdAndMemberId(Long studyId, Long memberId);

    @Query("""
            select sm from StudyMember sm join fetch sm.member
            where sm.study.id = :studyId and sm.status = :status
            order by sm.role asc, sm.joinedAt asc, sm.id asc
            """)
    List<StudyMember> findByStudyIdAndStatus(Long studyId, ParticipationStatus status);

    @Query("""
            select sm from StudyMember sm join fetch sm.study
            where sm.member.id = :memberId and sm.status = :status
            order by sm.joinedAt desc, sm.id desc
            """)
    List<StudyMember> findByMemberIdAndStatus(Long memberId, ParticipationStatus status);

    @Query("""
            select sm from StudyMember sm join fetch sm.member
            where sm.study.id in :studyIds and sm.role = :role and sm.status = :status
            """)
    List<StudyMember> findByStudyIdInAndRoleAndStatus(Collection<Long> studyIds, StudyRole role,
                                                      ParticipationStatus status);
}
