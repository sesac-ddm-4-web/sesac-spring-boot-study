package com.kdongd.shortari.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateDisplayNameException extends RuntimeException {

    public DuplicateDisplayNameException() {
        super("이미 사용 중인 이름입니다.");
    }
}