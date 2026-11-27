package com.example.study.common.auth;

import com.example.study.common.exception.BusinessException;
import com.example.study.common.exception.ErrorCode;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/** 컨트롤러 실행 전(입력값 검증 전)에 로그인 여부를 확인한다. */
public class LoginCheckInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 컨트롤러가 아닌 요청(없는 URL 등)은 그대로 넘겨 404가 되게 한다.
        if (!(handler instanceof HandlerMethod method) || method.hasMethodAnnotation(PublicApi.class)) {
            return true;
        }
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(SessionConst.MEMBER_ID) == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return true;
    }
}
