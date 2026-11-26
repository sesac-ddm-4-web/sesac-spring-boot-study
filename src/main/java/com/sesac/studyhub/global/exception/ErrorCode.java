package com.sesac.studyhub.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 공통
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다."),

    // 인증·인가
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않거나 만료된 토큰입니다."),
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "권한이 없습니다."),

    // 회원
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),

    // 스터디
    STUDY_NOT_FOUND(HttpStatus.NOT_FOUND, "스터디를 찾을 수 없습니다."),
    STUDY_CLOSED(HttpStatus.BAD_REQUEST, "모집이 마감된 스터디입니다."),
    STUDY_FULL(HttpStatus.CONFLICT, "스터디 정원이 가득 찼습니다."),
    ALREADY_JOINED(HttpStatus.CONFLICT, "이미 참여한 스터디입니다."),
    NOT_STUDY_MEMBER(HttpStatus.NOT_FOUND, "참여하지 않은 스터디입니다."),
    LEADER_CANNOT_LEAVE(HttpStatus.BAD_REQUEST, "스터디장은 탈퇴할 수 없습니다."),
    NOT_STUDY_LEADER(HttpStatus.FORBIDDEN, "스터디장만 가능한 작업입니다."),

    // 일정
    INVALID_SCHEDULE_PERIOD(HttpStatus.BAD_REQUEST, "일정 종료 시간은 시작 시간 이후여야 합니다.");

    private final HttpStatus status;
    private final String message;

}
