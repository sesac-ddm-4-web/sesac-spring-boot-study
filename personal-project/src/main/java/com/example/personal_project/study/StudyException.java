package com.example.personal_project.study;

public class StudyException extends RuntimeException {
    public final int status;
    public final String code;
    public StudyException(int status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }
    public static StudyException missing() { return new StudyException(404, "NOT_FOUND", "대상을 찾을 수 없습니다."); }
    public static StudyException forbidden() { return new StudyException(403, "FORBIDDEN", "이 작업을 수행할 권한이 없습니다."); }
}
