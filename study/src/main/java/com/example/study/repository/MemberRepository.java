package com.example.study.repository;

import java.util.Optional;

import com.example.study.domain.Member;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberRepository extends JpaRepository<Member, Long> {

    boolean existsByEmail(String email);

    @EntityGraph(attributePaths = "profile")
    Optional<Member> findByEmail(String email);

    /** 프로필이 필요한 조회는 한 번의 join으로 가져온다. */
    @EntityGraph(attributePaths = "profile")
    Optional<Member> findWithProfileById(Long id);

    /** {@code SELECT ... FOR UPDATE}. 스터디보다 회원을 먼저 잠근다. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from Member m where m.id = :id")
    Optional<Member> findByIdForUpdate(@Param("id") Long id);
}
