package com.example.study.service;

import java.nio.charset.StandardCharsets;

import com.example.study.common.exception.BusinessException;
import com.example.study.common.exception.ErrorCode;
import com.example.study.dto.LoginRequest;
import com.example.study.dto.MemberResponse;
import com.example.study.repository.MemberRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /** 이메일과 비밀번호를 확인한다. 어느 쪽이 틀렸는지는 알려주지 않는다. */
    public MemberResponse authenticate(LoginRequest request) {
        // BCrypt는 72바이트까지만 다루므로 더 긴 값은 일치할 수 없다.
        if (request.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }
        return memberRepository.findByEmail(request.email())
                .filter(member -> passwordEncoder.matches(request.password(), member.getPassword()))
                .map(MemberResponse::from)
                .orElseThrow(() -> new BusinessException(ErrorCode.LOGIN_FAILED));
    }
}
