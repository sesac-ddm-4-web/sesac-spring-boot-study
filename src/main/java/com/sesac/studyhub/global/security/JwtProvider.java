package com.sesac.studyhub.global.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtProvider {

    private final SecretKey key;
    private final long accessValidityMs;

    public JwtProvider(@Value("${jwt.secret}") String secret,
        @Value("${jwt.access-validity-ms}") long accessValidityMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)); // 32바이트 미만이면 기동 실패
        this.accessValidityMs = accessValidityMs;
    }

    public String createAccessToken(Long memberId) {
        Date now = new Date();

        return Jwts.builder()
            .subject(String.valueOf(memberId))
            .issuedAt(now)
            .expiration(new Date(now.getTime() + accessValidityMs))
            .signWith(key)
            .compact();
    }

    /** 서명 불일치·만료·형식 오류면 JwtException, 빈 문자열이면 IllegalArgumentException */
    public Long getMemberId(String token) {
        String subject = Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .getSubject();
        return Long.valueOf(subject);
    }

}
