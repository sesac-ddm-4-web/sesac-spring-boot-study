package com.example.study.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Profile {

    public static final int INTRODUCTION_MAX_LENGTH = 500;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = INTRODUCTION_MAX_LENGTH)
    private String introduction;

    protected Profile() {
    }

    Profile(String introduction) {
        changeIntroduction(introduction);
    }

    void changeIntroduction(String introduction) {
        // 자기소개는 선택 항목이므로 없으면 빈 문자열로 저장한다.
        this.introduction = introduction == null ? "" : introduction;
    }

    public Long getId() {
        return id;
    }

    public String getIntroduction() {
        return introduction;
    }
}
