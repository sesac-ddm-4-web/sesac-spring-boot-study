package com.example.sesac_spring_boot_study.infarstructrue;

import com.example.sesac_spring_boot_study.domain.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProfileRepository extends JpaRepository<Profile, Long> {

    Optional<Profile> findByMemberId(Long memberId);
}
