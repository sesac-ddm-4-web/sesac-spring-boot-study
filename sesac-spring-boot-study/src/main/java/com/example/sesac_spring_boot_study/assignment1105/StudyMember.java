package com.example.sesac_spring_boot_study.assignment1105;

public class StudyMember {
    //id·이름·확정/대기 상태
    private long id;
    private String name;
    private boolean confirmed; // true가 확정 false 대기

    public StudyMember(long id, String name, boolean confirmed) {
        this.id = id;
        this.name = name;
        this.confirmed = confirmed;
    }

    public long getId() { return id; }
    public String getName() { return name; }
    public boolean isConfirmed() { return confirmed; }
    public void setConfirmed(boolean confirmed) { this.confirmed = confirmed; }
}
