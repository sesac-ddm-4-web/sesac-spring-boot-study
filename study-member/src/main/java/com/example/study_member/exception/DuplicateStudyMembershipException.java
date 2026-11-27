package com.example.study_member.exception;

public class DuplicateStudyMembershipException extends ConflictException {

    public DuplicateStudyMembershipException() {
        super("이미 참여한 스터디입니다.");
    }
}
