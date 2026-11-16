package com.example.personal_project.domain.url.exception;



/** 단축 코드 UNIQUE 제약 충돌만 나타낸다. */
public class ShortLinkCodeConflictException extends RuntimeException {
    public ShortLinkCodeConflictException(Throwable cause) {
        super("단축 코드가 이미 사용 중입니다.", cause);
    }
}
