package com.example.personal_project.common.application;

public class ServiceException extends RuntimeException {

    public enum Code {
        ACTOR_REQUIRED("X-Member-Id 헤더가 필요합니다."),
        MEMBER_NOT_FOUND("회원을 찾을 수 없습니다."),
        STUDY_NOT_FOUND("스터디를 찾을 수 없습니다."),
        MEMBERSHIP_NOT_FOUND("스터디 참여 이력이 없습니다."),
        OWNER_REQUIRED("스터디장만 수행할 수 있습니다."),
        ACTIVE_MEMBERSHIP_REQUIRED("현재 참여 중인 회원만 조회할 수 있습니다."),
        EMAIL_ALREADY_EXISTS("이미 등록된 이메일입니다."),
        ALREADY_JOINED("이미 참여 중인 스터디입니다."),
        REJOIN_NOT_ALLOWED("탈퇴한 스터디에는 재가입할 수 없습니다."),
        RECRUITMENT_CLOSED("스터디 모집이 닫혔습니다."),
        STUDY_FULL("스터디 정원이 찼습니다."),
        OWNER_CANNOT_LEAVE("스터디장은 탈퇴할 수 없습니다.");

        private final String message;

        Code(String message) {
            this.message = message;
        }
    }

    private final Code code;

    public ServiceException(Code code) {
        this(code, null);
    }

    public ServiceException(Code code, Throwable cause) {
        super(code.message, cause);
        this.code = code;
    }

    public Code getCode() {
        return code;
    }
}
