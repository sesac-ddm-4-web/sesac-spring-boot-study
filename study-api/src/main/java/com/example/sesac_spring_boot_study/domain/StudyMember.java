package com.example.sesac_spring_boot_study.domain;

public class StudyMember {

    private Long id;
    private String name;
    private boolean confirm;

    public StudyMember(Long id, String name, boolean confirm) {
        this.id = id;
        this.name = name;
        this.confirm = confirm;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isConfirm() {
        return confirm;
    }

    public void setConfirmed(boolean confirm) {
        this.confirm = confirm;
    }
}
