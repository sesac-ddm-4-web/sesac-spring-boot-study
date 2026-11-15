package com.work.shortenurl.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    INVALID_INPUT(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다."),
    INVALID_URL(HttpStatus.BAD_REQUEST, "URL 형식이 올바르지 않습니다."),
    SELF_REFERENCE_URL(HttpStatus.BAD_REQUEST, "이 서비스의 단축 URL은 다시 단축할 수 없습니다."),
    INVALID_REQUEST_BODY(HttpStatus.BAD_REQUEST, "요청 본문을 읽을 수 없습니다."),
    SHORTEN_URL_NOT_FOUND(HttpStatus.NOT_FOUND, "단축 URL을 찾을 수 없습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 경로를 찾을 수 없습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "지원하지 않는 HTTP 메서드입니다."),
    KEY_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "단축 URL 생성에 실패했습니다. 잠시 후 다시 시도해주세요."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String message;

}