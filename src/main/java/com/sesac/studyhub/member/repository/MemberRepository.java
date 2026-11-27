package com.sesac.studyhub.member.repository;

import com.sesac.studyhub.member.domain.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberRepository extends JpaRepository<Member, Long> {

    boolean existsByEmail(String email);

    Optional<Member> findByEmail(String email);

    @Query("select m from Member m join fetch m.profile where m.id = :id")
    Optional<Member> findWithProfileById(@Param("id") Long id);

}
