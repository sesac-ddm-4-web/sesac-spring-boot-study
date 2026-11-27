package com.sesac.studyhub;

import com.sesac.studyhub.global.security.JwtProvider;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtProviderTest {

    private static final String SECRET = "test-secret-key-must-be-at-least-32-bytes-long";

    @Test
    void 토큰을_발급하고_memberId를_꺼낼_수_있다() {
        JwtProvider provider = new JwtProvider(SECRET, 60_000);

        String token = provider.createAccessToken(1L);

        assertThat(provider.getMemberId(token)).isEqualTo(1L);
    }

    @Test
    void 만료된_토큰은_예외가_발생한다() {
        JwtProvider provider = new JwtProvider(SECRET, -1_000);   // 이미 만료
        String token = provider.createAccessToken(1L);

        assertThatThrownBy(() -> provider.getMemberId(token))
            .isInstanceOf(JwtException.class);
    }

    @Test
    void 다른_키로_서명된_토큰은_예외가_발생한다() {
        JwtProvider other = new JwtProvider("another-secret-key-must-be-at-least-32-bytes", 60_000);
        String token = other.createAccessToken(1L);

        JwtProvider provider = new JwtProvider(SECRET, 60_000);
        assertThatThrownBy(() -> provider.getMemberId(token))
            .isInstanceOf(JwtException.class);
    }
}