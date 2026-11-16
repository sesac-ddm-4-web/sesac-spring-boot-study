package com.example.personal_project.domain.url.model;



public record ShortCode(String value) {
    public ShortCode {
        if (value == null) throw new IllegalArgumentException("단축 코드가 필요합니다.");
        value = value.trim();
        if (value.length() < 3 || value.length() > 40 || !value.matches("^[a-z0-9]+(?:-[a-z0-9]+)*$"))
            throw new IllegalArgumentException("단축 코드는 3~40자의 소문자·숫자·하이픈 형식이어야 합니다.");
    }
}
