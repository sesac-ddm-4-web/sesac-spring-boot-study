package com.example.study_member.exception;

public class MemberNotFoundException extends ItemNotFoundException {
    public MemberNotFoundException(String message) {
        super(message);
    }

    public MemberNotFoundException(Long memberId) {
        this("%d id의 회원이 없습니다.".formatted(memberId));
    }
}
