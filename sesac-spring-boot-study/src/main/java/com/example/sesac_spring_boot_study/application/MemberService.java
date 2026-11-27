package com.example.sesac_spring_boot_study.application;

import com.example.sesac_spring_boot_study.domain.Member;
import com.example.sesac_spring_boot_study.domain.Profile;
import com.example.sesac_spring_boot_study.exception.BusinessException;
import com.example.sesac_spring_boot_study.exception.ErrorCode;
import com.example.sesac_spring_boot_study.infarstructrue.MemberRepository;
import com.example.sesac_spring_boot_study.infarstructrue.ProfileRepository;
import com.example.sesac_spring_boot_study.presentation.MemberDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final ProfileRepository profileRepository;

    public MemberService(MemberRepository memberRepository, ProfileRepository profileRepository) {
        this.memberRepository = memberRepository;
        this.profileRepository = profileRepository;
    }

    @Transactional
    public MemberDto.Response signUp(String email, String nickname) {
        if (memberRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.CONFLICT, "이미 사용 중인 이메일입니다.");
        }
        Member member = memberRepository.save(new Member(email, nickname));
        return MemberDto.Response.of(member, null);
    }

    public MemberDto.Response getMember(Long memberId) {
        Member member = findMember(memberId);
        Profile profile = profileRepository.findByMemberId(memberId).orElse(null);
        return MemberDto.Response.of(member, profile);
    }

    @Transactional
    public MemberDto.Response updateIntroduction(Long memberId, String introduction) {
        Member member = findMember(memberId);
        Profile profile = profileRepository.findByMemberId(memberId)
                .orElseGet(() -> profileRepository.save(new Profile(member, introduction)));
        profile.updateIntroduction(introduction);
        return MemberDto.Response.of(member, profile);
    }

    public Member findMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "회원을 찾을 수 없습니다."));
    }
}
