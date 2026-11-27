package com.example.sesac_spring_boot_study.infarstructrue;

import com.example.sesac_spring_boot_study.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
    boolean existsByEmail(String email);
}
