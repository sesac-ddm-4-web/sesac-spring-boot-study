package com.example.sesac_spring_boot_study.domain;

public class StudyMember {
    private final int id;
    private final String name;
    private boolean confirmed;

    public StudyMember(Integer id, String name, boolean confirmed) {
        this.id = id;
        this.name = name;
        this.confirmed = confirmed;
    }

    public String getName() {
        return name;
    }

    public boolean isWaiting() {
        return !confirmed;
    }

    public void confirm() {
        confirmed = true;
    }

    @Override
    public String toString() {
        return String.format("[ id=%d, 이름=%s, 상태=%s ]", id, name, confirmed ? "확정" : "대기");
    }
}
