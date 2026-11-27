package com.example.personal_project.common;

public class InvalidDomainFieldException extends IllegalArgumentException {

    private final String field;

    public InvalidDomainFieldException(String field, String message) {
        super(message);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
