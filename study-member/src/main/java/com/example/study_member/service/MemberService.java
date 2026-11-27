package com.example.study_member.service;

import com.example.study_member.domain.Member;
import com.example.study_member.domain.MemberProfile;
import com.example.study_member.exception.MemberNotFoundException;
import com.example.study_member.repository.MemberProfileRepository;
import com.example.study_member.repository.MemberRepository;
import com.example.study_member.dto.request.CreateMemberRequest;
import com.example.study_member.dto.request.UpdateMemberProfileRequest;
import com.example.study_member.dto.response.MemberDetailResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MemberService {
    private final MemberRepository memberRepository;
    private final MemberProfileRepository memberProfileRepository;

    public MemberService(MemberRepository memberRepository, MemberProfileRepository memberProfileRepository) {
        this.memberRepository = memberRepository;
        this.memberProfileRepository = memberProfileRepository;
    }

    @Transactional
    public MemberDetailResponse createMember(CreateMemberRequest request) {
        Member member = new Member(request.name());
        MemberProfile profile = new MemberProfile(member, request.description());

        Member createdMember = memberRepository.save(member);
        MemberProfile createdProfile = memberProfileRepository.save(profile);

        return new MemberDetailResponse(
                createdMember.getId(),
                createdMember.getName(),
                createdProfile.getDescription()
        );
    }

    @Transactional
    public MemberDetailResponse updateMemberProfile(Long memberId, UpdateMemberProfileRequest request) {
        MemberProfile profile = memberProfileRepository.findByMemberId(memberId)
                .orElseThrow(() -> new MemberNotFoundException("%d id의 멤버가 없습니다.".formatted(memberId)));
        profile.updateDescription(request.description());
        return MemberDetailResponse.from(profile);
    }

    @Transactional(readOnly = true)
    public MemberDetailResponse getMemberDetail(Long memberId) {
        MemberProfile profile = memberProfileRepository.findByMemberId(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId));
        return MemberDetailResponse.from(profile);
    }
}
