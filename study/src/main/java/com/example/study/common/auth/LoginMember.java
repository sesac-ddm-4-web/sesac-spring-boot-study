package com.example.study.common.auth;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/** 로그인한 회원의 ID({@code Long})를 주입받는다. */
@Target(PARAMETER)
@Retention(RUNTIME)
public @interface LoginMember {
}
