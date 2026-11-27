package com.example.studyservice.global.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    INVALID_INPUT(HttpStatus.BAD_REQUEST, "%s"),
    INVALID_REQUEST_BODY(HttpStatus.BAD_REQUEST, "요청 본문 형식이 올바르지 않습니다."),
    INVALID_PARAMETER_TYPE(HttpStatus.BAD_REQUEST, "%s: 형식이 올바르지 않습니다."),
    MISSING_PARAMETER(HttpStatus.BAD_REQUEST, "%s: 필수 값입니다."),

    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다. 토큰을 재발급해 주세요."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않거나 만료된 리프레시 토큰입니다. 다시 로그인해 주세요."),
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),

    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "%d번 회원이 없습니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "email: '%s'는 이미 가입된 이메일입니다."),

    STUDY_NOT_FOUND(HttpStatus.NOT_FOUND, "%d번 스터디가 없습니다."),
    STUDY_CLOSED(HttpStatus.CONFLICT, "종료된 스터디입니다."),
    STUDY_FULL(HttpStatus.CONFLICT, "정원(%d명)이 가득 찬 스터디입니다."),
    ALREADY_JOINED(HttpStatus.CONFLICT, "이미 참여 중인 스터디입니다."),
    NOT_STUDY_MEMBER(HttpStatus.FORBIDDEN, "스터디에 참여 중인 회원만 할 수 있습니다."),
    NOT_STUDY_LEADER(HttpStatus.FORBIDDEN, "스터디장만 할 수 있습니다."),
    LEADER_MUST_DELEGATE(HttpStatus.CONFLICT, "다른 멤버가 남아 있어 스터디장은 탈퇴할 수 없습니다. 먼저 스터디장을 위임해 주세요."),
    CANNOT_DELEGATE_TO_SELF(HttpStatus.BAD_REQUEST, "memberId: 자기 자신에게는 위임할 수 없습니다."),
    DELEGATE_TARGET_NOT_MEMBER(HttpStatus.BAD_REQUEST, "memberId: %d번 회원은 이 스터디에 참여 중이 아닙니다."),
    CAPACITY_BELOW_MEMBER_COUNT(HttpStatus.BAD_REQUEST, "capacity: 현재 인원(%d명)보다 적게 줄일 수 없습니다."),

    SCHEDULE_NOT_FOUND(HttpStatus.NOT_FOUND, "%d번 일정이 이 스터디에 없습니다."),
    INVALID_SCHEDULE_TIME(HttpStatus.BAD_REQUEST, "endAt: 시작 시각(startAt)보다 뒤여야 합니다.");

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
