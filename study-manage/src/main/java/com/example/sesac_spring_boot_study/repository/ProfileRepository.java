package com.example.sesac_spring_boot_study.repository;

import com.example.sesac_spring_boot_study.domain.Profile;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
    Optional<Profile> findByMemberId(Long memberId);
}
