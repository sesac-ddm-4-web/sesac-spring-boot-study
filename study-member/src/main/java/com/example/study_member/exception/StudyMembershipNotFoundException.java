package com.example.study_member.exception;

public class StudyMembershipNotFoundException extends ItemNotFoundException {
    public StudyMembershipNotFoundException(String message) {
        super(message);
    }

    public StudyMembershipNotFoundException(Long studyId, Long memberId) {

        this("id가 %d인 스터디에는 id가 %d인 회원이 없습니다.".formatted(studyId, memberId));
    }
}
