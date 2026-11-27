package com.sesac.studyhub.global.security;

import com.sesac.studyhub.global.exception.ErrorCode;
import com.sesac.studyhub.global.exception.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
        AuthenticationException authException) throws IOException {
        // 토큰이 있었는데 무효 → INVALID_TOKEN, 토큰 자체가 없음 → UNAUTHORIZED
        Object jwtError = request.getAttribute(JwtAuthenticationFilter.JWT_ERROR_ATTRIBUTE);
        ErrorCode errorCode = (jwtError instanceof ErrorCode code) ? code : ErrorCode.UNAUTHORIZED;

        response.setStatus(errorCode.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(objectMapper.writeValueAsString(ErrorResponse.of(errorCode)));
    }


}
