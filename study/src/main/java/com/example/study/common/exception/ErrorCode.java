package com.example.study.common.exception;

import org.springframework.http.HttpStatus;

/**
 * 오류의 상태 코드와 안내 문구를 한 곳에서 관리한다. 업무 오류는 각 단계에서 추가한다.
 */
public enum ErrorCode {

    INVALID_INPUT(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 대상을 찾을 수 없습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "지원하지 않는 요청 방식입니다."),
    UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "지원하지 않는 Content-Type입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "권한이 없습니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버에서 오류가 발생했습니다."),

    // 업무 오류. 같은 상태 코드의 공통 오류가 위에 있어야 한다. (GlobalExceptionHandler가 앞에서부터 찾음)
    NOTHING_TO_UPDATE(HttpStatus.BAD_REQUEST, "수정할 이름 또는 자기소개를 입력해 주세요."),
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    INVALID_STUDY_TIME(HttpStatus.BAD_REQUEST, "시작 시각은 종료 시각보다 빨라야 합니다."),
    NOT_STUDY_OWNER(HttpStatus.FORBIDDEN, "스터디장만 할 수 있습니다."),
    PAST_SCHEDULE(HttpStatus.BAD_REQUEST, "현재보다 이후의 일시만 등록할 수 있습니다."),
    SCHEDULE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "스터디장과 참여자만 일정을 볼 수 있습니다."),
    OWNER_CANNOT_LEAVE(HttpStatus.FORBIDDEN, "스터디장은 탈퇴할 수 없습니다. 스터디를 삭제해 주세요."),
    NOT_PARTICIPANT(HttpStatus.NOT_FOUND, "참여 중인 스터디가 아닙니다."),
    ALREADY_JOINED(HttpStatus.CONFLICT, "이미 참여 중인 스터디입니다."),
    RECRUITMENT_CLOSED(HttpStatus.CONFLICT, "모집이 마감된 스터디입니다."),
    STUDY_FULL(HttpStatus.CONFLICT, "정원이 가득 찼습니다."),
    STUDY_NOT_FOUND(HttpStatus.NOT_FOUND, "스터디를 찾을 수 없습니다."),
    TIME_OVERLAP(HttpStatus.CONFLICT, "기존 스터디 시간과 겹칩니다."),
    CONCURRENT_MODIFICATION(HttpStatus.CONFLICT, "다른 요청과 겹쳤습니다. 잠시 후 다시 시도해 주세요.");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus status() {
        return status;
    }

    public String message() {
        return message;
    }
}
