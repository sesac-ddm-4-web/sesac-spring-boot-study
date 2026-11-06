package com.example.sesac_spring_boot_study.entity;

public class StudyMember {

    public long getMemberId() {
        return memberId;
    }

    public void setMemberId(long memberId) {
        this.memberId = memberId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public StudyMember(long memberId, String name, String state) {
        this.memberId = memberId;
        this.name = name;
        this.state = state;
    }

    private long memberId;
    private String name;
    private String state;
}
