package com.example.studyservice.member.service;

import com.example.studyservice.global.exception.ApiException;
import com.example.studyservice.global.exception.ErrorCode;
import com.example.studyservice.member.domain.Profile;
import com.example.studyservice.member.dto.req.ProfileUpdateReq;
import com.example.studyservice.member.dto.res.MemberProfileRes;
import com.example.studyservice.member.dto.res.MemberRes;
import com.example.studyservice.member.dto.res.ProfileRes;
import com.example.studyservice.member.repository.ProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
@Transactional(readOnly = true)
public class MemberService {

    private final ProfileRepository profileRepository;
    private final Clock clock;

    public MemberService(ProfileRepository profileRepository, Clock clock) {
        this.profileRepository = profileRepository;
        this.clock = clock;
    }

    public MemberRes findMe(Long memberId) {
        Profile profile = findProfile(memberId);
        return MemberRes.of(profile.getMember(), profile);
    }

    public MemberProfileRes findProfileOf(Long memberId) {
        return MemberProfileRes.of(findProfile(memberId));
    }

    @Transactional
    public ProfileRes updateProfile(Long memberId, ProfileUpdateReq request) {
        Profile profile = findProfile(memberId);
        profile.update(request.nickname(), request.bio(), request.interests(), LocalDateTime.now(clock));
        return ProfileRes.of(profile);
    }

    private Profile findProfile(Long memberId) {
        return profileRepository.findByMemberId(memberId)
                .orElseThrow(() -> new ApiException(ErrorCode.MEMBER_NOT_FOUND, memberId));
    }
}
