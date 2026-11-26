package com.example.personal_project.domain.study.exception;



public class StudyException extends RuntimeException {
    public enum Reason { NOT_FOUND, FORBIDDEN, INVALID_PRESENTER, DUPLICATE_SESSION_NO }
    private final Reason reason;
    public StudyException(Reason reason, String message) { super(message); this.reason = reason; }
    public Reason reason() { return reason; }
    public static StudyException missing() { return new StudyException(Reason.NOT_FOUND, "대상을 찾을 수 없습니다."); }
    public static StudyException forbidden() { return new StudyException(Reason.FORBIDDEN, "이 작업을 수행할 권한이 없습니다."); }
    public static StudyException invalidPresenter() { return new StudyException(Reason.INVALID_PRESENTER, "발표자 계정만 배정할 수 있습니다."); }
    public static StudyException duplicateSessionNo() { return new StudyException(Reason.DUPLICATE_SESSION_NO, "이미 등록된 회차 번호입니다."); }
}
