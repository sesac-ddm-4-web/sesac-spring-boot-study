package com.sesac.studyhub.auth.service;

import com.sesac.studyhub.auth.dto.LoginRequest;
import com.sesac.studyhub.auth.dto.SignupRequest;
import com.sesac.studyhub.auth.dto.TokenResponse;
import com.sesac.studyhub.global.exception.BusinessException;
import com.sesac.studyhub.global.exception.ErrorCode;
import com.sesac.studyhub.global.security.JwtProvider;
import com.sesac.studyhub.member.domain.Member;
import com.sesac.studyhub.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Transactional
    public Long signup(SignupRequest request) {
        if (memberRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }

        Member member = Member.create(
            request.email(),
            passwordEncoder.encode(request.password()),
            request.name()
        );

        return memberRepository.save(member).getId();
    }

    public TokenResponse login(LoginRequest request) {
        Member member = memberRepository.findByEmail(request.email())
            .filter(m -> passwordEncoder.matches(request.password(), m.getPassword()))
            .orElseThrow(() -> new BusinessException(ErrorCode.LOGIN_FAILED));

        return TokenResponse.bearer(jwtProvider.createAccessToken(member.getId()));
    }
}
