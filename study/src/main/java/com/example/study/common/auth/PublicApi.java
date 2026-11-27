package com.example.study.common.auth;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/** 로그인 없이 호출할 수 있는 API. 표시가 없으면 로그인이 필요하다. */
@Target(METHOD)
@Retention(RUNTIME)
public @interface PublicApi {
}
