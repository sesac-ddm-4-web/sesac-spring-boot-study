package com.example.studyservice.auth.service;

import com.example.studyservice.auth.domain.RefreshToken;
import com.example.studyservice.auth.dto.req.LoginReq;
import com.example.studyservice.auth.dto.req.ReissueReq;
import com.example.studyservice.auth.dto.req.SignupReq;
import com.example.studyservice.auth.dto.res.TokenRes;
import com.example.studyservice.auth.repository.RefreshTokenRepository;
import com.example.studyservice.global.exception.ApiException;
import com.example.studyservice.global.exception.ErrorCode;
import com.example.studyservice.global.security.JwtProvider;
import com.example.studyservice.member.domain.Member;
import com.example.studyservice.member.domain.Profile;
import com.example.studyservice.member.dto.res.MemberRes;
import com.example.studyservice.member.repository.MemberRepository;
import com.example.studyservice.member.repository.ProfileRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
@Transactional
public class AuthService {

    private final MemberRepository memberRepository;
    private final ProfileRepository profileRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final Clock clock;

    public AuthService(MemberRepository memberRepository, ProfileRepository profileRepository,
                       RefreshTokenRepository refreshTokenRepository, PasswordEncoder passwordEncoder,
                       JwtProvider jwtProvider, Clock clock) {
        this.memberRepository = memberRepository;
        this.profileRepository = profileRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
        this.clock = clock;
    }

    public MemberRes signup(SignupReq request) {
        String email = Member.normalizeEmail(request.email());
        if (memberRepository.existsByEmail(email)) {
            throw new ApiException(ErrorCode.DUPLICATE_EMAIL, email);
        }
        LocalDateTime now = LocalDateTime.now(clock);
        Member member = new Member(email, passwordEncoder.encode(request.password()), request.name(), now);
        try {
            memberRepository.saveAndFlush(member);
        } catch (DataIntegrityViolationException e) {
            throw new ApiException(ErrorCode.DUPLICATE_EMAIL, email);
        }
        String nickname = request.nickname() == null || request.nickname().isBlank()
                ? member.getName() : request.nickname();
        Profile profile = profileRepository.save(new Profile(member, nickname, now));
        return MemberRes.of(member, profile);
    }

    public TokenRes login(LoginReq request) {
        Member member = memberRepository.findByEmail(Member.normalizeEmail(request.email()))
                .filter(found -> passwordEncoder.matches(request.password(), found.getPassword()))
                .orElseThrow(() -> new ApiException(ErrorCode.LOGIN_FAILED));

        String refreshToken = jwtProvider.createRefreshToken();
        refreshTokenRepository.findByMemberId(member.getId())
                .ifPresentOrElse(
                        saved -> saved.rotate(refreshToken, jwtProvider.refreshTokenExpiresAt()),
                        () -> refreshTokenRepository.save(
                                new RefreshToken(member, refreshToken, jwtProvider.refreshTokenExpiresAt())));
        return issue(member.getId(), refreshToken);
    }

    public TokenRes reissue(ReissueReq request) {
        RefreshToken saved = refreshTokenRepository.findByToken(request.refreshToken())
                .orElseThrow(() -> new ApiException(ErrorCode.INVALID_REFRESH_TOKEN));
        if (saved.isExpired(clock.instant())) {
            refreshTokenRepository.delete(saved);
            throw new ApiException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
        String refreshToken = jwtProvider.createRefreshToken();
        saved.rotate(refreshToken, jwtProvider.refreshTokenExpiresAt());
        return issue(saved.getMember().getId(), refreshToken);
    }

    public void logout(Long memberId) {
        refreshTokenRepository.findByMemberId(memberId).ifPresent(refreshTokenRepository::delete);
    }

    private TokenRes issue(Long memberId, String refreshToken) {
        return TokenRes.bearer(jwtProvider.createAccessToken(memberId),
                jwtProvider.accessTokenExpiresInSeconds(), refreshToken);
    }
}
