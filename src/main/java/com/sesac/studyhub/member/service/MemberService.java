package com.sesac.studyhub.member.service;

import com.sesac.studyhub.global.exception.BusinessException;
import com.sesac.studyhub.global.exception.ErrorCode;
import com.sesac.studyhub.member.domain.Member;
import com.sesac.studyhub.member.dto.MemberProfileResponse;
import com.sesac.studyhub.member.dto.MyInfoResponse;
import com.sesac.studyhub.member.dto.ProfileUpdateRequest;
import com.sesac.studyhub.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;

    public MyInfoResponse getMyInfo(Long memberId) {
        return MyInfoResponse.from(findMemberWithProfile(memberId));
    }

    public MemberProfileResponse getMemberProfile(Long memberId) {
        return MemberProfileResponse.from(findMemberWithProfile(memberId));
    }

    @Transactional
    public MyInfoResponse updateMyProfile(Long memberId, ProfileUpdateRequest request) {
        Member member = findMemberWithProfile(memberId);
        member.updateProfile(request.imageUrl(), request.description());
        return MyInfoResponse.from(member);
    }

    private Member findMemberWithProfile(Long memberId) {
        return memberRepository.findWithProfileById(memberId)
            .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }

}
