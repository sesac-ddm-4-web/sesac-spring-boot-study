package com.example.study_member.exception;

public class StudyCreatorCannotLeaveException extends ConflictException {

    public StudyCreatorCannotLeaveException() {
        super("스터디 개설자는 탈퇴할 수 없습니다.");
    }
}
