package com.example.study_member.exception;

public class StudyNotFoundException extends ItemNotFoundException {
    public StudyNotFoundException(String message) {
        super(message);
    }

    public StudyNotFoundException(Long memberId) {
        this("%d id의 스터디가 없습니다.".formatted(memberId));
    }
}
