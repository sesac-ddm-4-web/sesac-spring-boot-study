package com.example.personal_project.study.repository;

import com.example.personal_project.study.domain.MembershipRole;
import com.example.personal_project.study.domain.MembershipStatus;
import com.example.personal_project.study.domain.StudyMembership;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface StudyMembershipRepository extends JpaRepository<StudyMembership, Long> {

    Optional<StudyMembership> findByStudy_IdAndMember_Id(long studyId, long memberId);

    long countByStudy_IdAndStatus(long studyId, MembershipStatus status);

    boolean existsByStudy_IdAndMember_IdAndStatus(long studyId, long memberId, MembershipStatus status);

    @Query("""
            select membership.study.id as studyId,
                   max(case when membership.role = :ownerRole then membership.member.id else null end) as ownerMemberId,
                   count(membership.id) as activeMemberCount
            from StudyMembership membership
            where membership.study.id in :studyIds and membership.status = :activeStatus
            group by membership.study.id
            """)
    List<ActiveStats> findActiveStats(
            @Param("studyIds") Collection<Long> studyIds,
            @Param("activeStatus") MembershipStatus activeStatus,
            @Param("ownerRole") MembershipRole ownerRole);

    @Query(value = """
            select membership.id as membershipId, membership.member.id as memberId, profile.nickname as nickname,
                   membership.role as role, membership.joinedAt as joinedAt
            from StudyMembership membership
            join MemberProfile profile on profile.member = membership.member
            where membership.study.id = :studyId and membership.status = :status
            order by membership.joinedAt asc, membership.id asc
            """, countQuery = """
            select count(membership.id) from StudyMembership membership
            where membership.study.id = :studyId and membership.status = :status
            """)
    Page<ParticipantRow> findParticipants(
            @Param("studyId") long studyId, @Param("status") MembershipStatus status, Pageable page);

    @Query(value = """
            select membership from StudyMembership membership join fetch membership.study
            where membership.member.id = :memberId and membership.status = :status
            order by membership.joinedAt desc, membership.id desc
            """, countQuery = """
            select count(membership.id) from StudyMembership membership
            where membership.member.id = :memberId and membership.status = :status
            """)
    Page<StudyMembership> findMemberStudies(
            @Param("memberId") long memberId, @Param("status") MembershipStatus status, Pageable page);

    interface ActiveStats {
        Long getStudyId();
        Long getOwnerMemberId();
        long getActiveMemberCount();
    }

    interface ParticipantRow {
        long getMembershipId();
        long getMemberId();
        String getNickname();
        MembershipRole getRole();
        Instant getJoinedAt();
    }
}
