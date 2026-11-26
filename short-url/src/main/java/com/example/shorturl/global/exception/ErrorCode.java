package com.example.shorturl.global.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    INVALID_INPUT(HttpStatus.BAD_REQUEST, "%s"),
    INVALID_REQUEST_BODY(HttpStatus.BAD_REQUEST, "요청 본문 형식이 올바르지 않습니다."),
    INVALID_PARAMETER_TYPE(HttpStatus.BAD_REQUEST, "%s: 형식이 올바르지 않습니다."),
    MISSING_PARAMETER(HttpStatus.BAD_REQUEST, "%s: 필수 값입니다."),

    ORIGINAL_URL_REQUIRED(HttpStatus.BAD_REQUEST, "originalUrl: 필수 값입니다."),
    ORIGINAL_URL_TOO_LONG(HttpStatus.BAD_REQUEST, "originalUrl: %d자 이하여야 합니다."),
    INVALID_URL_FORMAT(HttpStatus.BAD_REQUEST, "originalUrl: URL 형식이 올바르지 않습니다."),
    UNSUPPORTED_URL_SCHEME(HttpStatus.BAD_REQUEST, "originalUrl: http 또는 https로 시작하는 절대 주소만 등록할 수 있습니다."),
    MISSING_URL_HOST(HttpStatus.BAD_REQUEST, "originalUrl: 도메인(host)이 없는 주소입니다."),
    SELF_REFERENCING_URL(HttpStatus.BAD_REQUEST, "originalUrl: 포트링크 자신의 주소는 등록할 수 없습니다."),

    INVALID_CUSTOM_KEY(HttpStatus.BAD_REQUEST, "customKey: 영문, 숫자, -, _ 로 4~30자여야 합니다."),
    RESERVED_CUSTOM_KEY(HttpStatus.BAD_REQUEST, "customKey: '%s'는 예약어라 사용할 수 없습니다."),
    PAST_EXPIRATION_DATE(HttpStatus.BAD_REQUEST, "expiresOn: 오늘 이후 날짜여야 합니다."),
    INVALID_STATS_DAYS(HttpStatus.BAD_REQUEST, "days: 1~%d 사이여야 합니다."),

    DISABLED_SHORT_URL(HttpStatus.FORBIDDEN, "사용 중지된 링크입니다."),
    SHORT_URL_NOT_FOUND(HttpStatus.NOT_FOUND, "'%s'에 해당하는 단축 URL이 없습니다."),
    DUPLICATE_CUSTOM_KEY(HttpStatus.CONFLICT, "customKey: '%s'는 이미 사용 중인 짧은 주소입니다."),
    EXPIRED_SHORT_URL(HttpStatus.GONE, "만료된 링크입니다. (만료일 %s)"),

    LACK_OF_SHORT_URL_KEY(HttpStatus.SERVICE_UNAVAILABLE, "단축 키를 만들지 못했습니다. 잠시 후 다시 시도해 주세요.");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public String format(Object... args) {
        return args.length == 0 ? message : String.format(message, args);
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
