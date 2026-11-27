package com.sesac.studyhub.auth.service;

import com.sesac.studyhub.auth.dto.LoginRequest;
import com.sesac.studyhub.auth.dto.SignupRequest;
import com.sesac.studyhub.auth.dto.TokenResponse;
import com.sesac.studyhub.global.exception.ErrorCode;
import com.sesac.studyhub.global.security.JwtProvider;
import com.sesac.studyhub.member.domain.Member;
import com.sesac.studyhub.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static com.sesac.studyhub.support.TestFixtures.assertBusinessError;
import static com.sesac.studyhub.support.TestFixtures.member;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock MemberRepository memberRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtProvider jwtProvider;
    @InjectMocks AuthService authService;

    @Test
    @DisplayName("회원가입 시 비밀번호는 암호화해서 저장한다")
    void signupEncodesPassword() {
        SignupRequest request = new SignupRequest("a@test.com", "password123", "서연");
        given(memberRepository.existsByEmail("a@test.com")).willReturn(false);
        given(passwordEncoder.encode("password123")).willReturn("ENCODED");
        given(memberRepository.save(any(Member.class))).willAnswer(invocation -> invocation.getArgument(0));

        authService.signup(request);

        ArgumentCaptor<Member> captor = ArgumentCaptor.forClass(Member.class);
        then(memberRepository).should().save(captor.capture());
        assertThat(captor.getValue().getPassword()).isEqualTo("ENCODED");
    }

    @Test
    @DisplayName("이미 가입된 이메일이면 DUPLICATE_EMAIL")
    void duplicateEmail() {
        given(memberRepository.existsByEmail("a@test.com")).willReturn(true);

        assertBusinessError(
            () -> authService.signup(new SignupRequest("a@test.com", "password123", "서연")),
            ErrorCode.DUPLICATE_EMAIL);
        then(memberRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("이메일·비밀번호가 맞으면 Bearer 토큰을 발급한다")
    void loginSuccess() {
        Member member = member(1L, "seoyeon");
        given(memberRepository.findByEmail("seoyeon@test.com")).willReturn(Optional.of(member));
        given(passwordEncoder.matches("password123", member.getPassword())).willReturn(true);
        given(jwtProvider.createAccessToken(1L)).willReturn("jwt-token");

        TokenResponse response = authService.login(new LoginRequest("seoyeon@test.com", "password123"));

        assertThat(response.accessToken()).isEqualTo("jwt-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
    }

    @Test
    @DisplayName("비밀번호가 틀리면 LOGIN_FAILED")
    void wrongPassword() {
        Member member = member(1L, "seoyeon");
        given(memberRepository.findByEmail("seoyeon@test.com")).willReturn(Optional.of(member));
        given(passwordEncoder.matches("wrong", member.getPassword())).willReturn(false);

        assertBusinessError(
            () -> authService.login(new LoginRequest("seoyeon@test.com", "wrong")),
            ErrorCode.LOGIN_FAILED);
    }

    @Test
    @DisplayName("없는 이메일도 같은 LOGIN_FAILED (가입 여부를 노출하지 않음)")
    void unknownEmail() {
        given(memberRepository.findByEmail("none@test.com")).willReturn(Optional.empty());

        assertBusinessError(
            () -> authService.login(new LoginRequest("none@test.com", "password123")),
            ErrorCode.LOGIN_FAILED);
    }
}