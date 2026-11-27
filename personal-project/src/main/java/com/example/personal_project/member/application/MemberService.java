package com.example.personal_project.member.application;

import com.example.personal_project.common.InvalidDomainFieldException;
import com.example.personal_project.common.application.ServiceException;
import com.example.personal_project.member.domain.Member;
import com.example.personal_project.member.domain.MemberProfile;
import com.example.personal_project.member.repository.MemberProfileRepository;
import com.example.personal_project.member.repository.MemberRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final MemberProfileRepository profileRepository;

    public MemberService(MemberRepository memberRepository, MemberProfileRepository profileRepository) {
        this.memberRepository = memberRepository;
        this.profileRepository = profileRepository;
    }

    @Transactional
    public Profile register(String email, String nickname, String bio) {
        Instant now = Instant.now();
        Member member = Member.register(email, now);
        MemberProfile profile = MemberProfile.create(member, nickname, bio, now);

        try {
            memberRepository.saveAndFlush(member);
        } catch (DataIntegrityViolationException exception) {
            if (isDuplicateEmail(exception)) {
                throw new ServiceException(ServiceException.Code.EMAIL_ALREADY_EXISTS, exception);
            }
            throw exception;
        }
        return Profile.from(profileRepository.save(profile));
    }

    @Transactional(readOnly = true)
    public Profile getProfile(long memberId) {
        if (memberId <= 0) {
            throw new InvalidDomainFieldException("memberId", "memberId must be a positive integer");
        }
        return Profile.from(requireProfile(memberId));
    }

    @Transactional
    public Profile updateProfile(long memberId, String nickname, String bio) {
        MemberProfile profile = requireProfile(memberId);
        profile.update(nickname, bio, Instant.now());
        return Profile.from(profile);
    }

    @Transactional(readOnly = true)
    public Member requireMember(long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new ServiceException(ServiceException.Code.MEMBER_NOT_FOUND));
    }

    private MemberProfile requireProfile(long memberId) {
        return profileRepository.findByMember_Id(memberId)
                .orElseThrow(() -> new ServiceException(ServiceException.Code.MEMBER_NOT_FOUND));
    }

    private static boolean isDuplicateEmail(DataIntegrityViolationException exception) {
        for (Throwable cause = exception.getCause(); cause != null; cause = cause.getCause()) {
            if (cause instanceof ConstraintViolationException violation) {
                String name = violation.getConstraintName();
                if (name != null && name.substring(name.lastIndexOf('.') + 1).equalsIgnoreCase(Member.EMAIL_UNIQUE_CONSTRAINT)) {
                    return true;
                }
            }
        }
        return false;
    }

    public record Profile(Long memberId, String nickname, String bio) {

        private static Profile from(MemberProfile profile) {
            return new Profile(profile.getMember().getId(), profile.getNickname(), profile.getBio());
        }
    }
}
