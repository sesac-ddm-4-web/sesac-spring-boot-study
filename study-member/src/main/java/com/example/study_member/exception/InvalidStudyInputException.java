package com.example.study_member.exception;

public class InvalidStudyInputException extends IllegalArgumentException {
    public InvalidStudyInputException(String message) {
        super(message);
    }
}
