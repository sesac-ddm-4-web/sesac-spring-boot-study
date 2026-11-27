package com.example.personal_project.common;

public final class DomainText {

    private DomainText() {
    }

    public static String requireLength(String value, String field, int minLength, int maxLength) {
        if (value == null) {
            throw new InvalidDomainFieldException(field, field + " is required");
        }

        String stripped = value.strip();
        int length = stripped.codePointCount(0, stripped.length());
        if (length < minLength || length > maxLength) {
            throw new InvalidDomainFieldException(field, field + " must be between " + minLength + " and " + maxLength + " characters");
        }
        return stripped;
    }
}
