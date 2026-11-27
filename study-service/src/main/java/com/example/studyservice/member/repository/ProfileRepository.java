package com.example.studyservice.member.repository;

import com.example.studyservice.member.domain.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ProfileRepository extends JpaRepository<Profile, Long> {

    @Query("select p from Profile p join fetch p.member where p.member.id = :memberId")
    Optional<Profile> findByMemberId(Long memberId);
}
