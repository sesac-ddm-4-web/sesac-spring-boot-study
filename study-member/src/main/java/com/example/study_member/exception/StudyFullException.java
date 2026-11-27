package com.example.study_member.exception;

public class StudyFullException extends ConflictException {
    public StudyFullException(String message) {
        super(message);
    }

    public StudyFullException(Long studyId, int capacity) {
        this("%d id의 스터디의 인원이 꽉 찼습니다. 정원은 %d명입니다.".formatted(studyId, capacity));
    }
}
