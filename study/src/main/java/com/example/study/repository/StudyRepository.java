package com.example.study.repository;

import java.util.List;
import java.util.Optional;

import com.example.study.domain.Study;
import com.example.study.dto.StudySummaryResponse;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudyRepository extends JpaRepository<Study, Long> {

    /** 목록 항목을 참여 인원과 함께 한 번의 쿼리로 가져온다. (N+1 방지) */
    String SELECT_SUMMARY = """
            select new com.example.study.dto.StudySummaryResponse(
                s.id, s.name, o.name, s.capacity, count(m.id),
                s.recruitStatus, s.dayOfWeek, s.startTime, s.endTime)
            from Study s
            join s.owner o
            left join s.members m
            """;

    String GROUP_AND_ORDER = """
             group by s.id, s.name, o.name, s.capacity, s.recruitStatus, s.dayOfWeek, s.startTime, s.endTime
             order by s.id desc
            """;

    @Query(SELECT_SUMMARY + GROUP_AND_ORDER)
    List<StudySummaryResponse> findAllSummaries();

    @Query(SELECT_SUMMARY + " where o.id = :memberId " + GROUP_AND_ORDER)
    List<StudySummaryResponse> findOwnedSummaries(@Param("memberId") Long memberId);

    /** 참여 중인 스터디. 본인이 개설한 스터디는 제외한다. */
    @Query(SELECT_SUMMARY + """
             where o.id <> :memberId
               and exists (select 1 from StudyMember mine
                           where mine.study = s and mine.member.id = :memberId)
            """ + GROUP_AND_ORDER)
    List<StudySummaryResponse> findJoinedSummaries(@Param("memberId") Long memberId);

    /** 회원이 개설했거나 참여 중인 모든 스터디. 시간 겹침 검사에 쓴다. */
    @Query("select sm.study from StudyMember sm where sm.member.id = :memberId")
    List<Study> findAllByParticipant(@Param("memberId") Long memberId);

    @EntityGraph(attributePaths = "owner")
    Optional<Study> findWithOwnerById(Long id);

    /** 엔티티를 영속성 컨텍스트에 올리지 않고 스터디장만 확인한다. */
    @Query("select s.owner.id from Study s where s.id = :id")
    Optional<Long> findOwnerId(@Param("id") Long id);

    /** {@code SELECT ... FOR UPDATE}. 회원을 잠근 뒤에 호출한다. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Study s where s.id = :id")
    Optional<Study> findByIdForUpdate(@Param("id") Long id);
}
