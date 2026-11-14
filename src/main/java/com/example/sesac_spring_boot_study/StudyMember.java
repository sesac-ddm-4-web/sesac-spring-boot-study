package com.example.sesac_spring_boot_study;

public class StudyMember {
    private long id;
    private String name;
    private boolean confirmed;

    public StudyMember(long id, String name, boolean confirmed) {
        this.id = id;
        this.name = name;
        this.confirmed = confirmed;
    }

    public long getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public boolean isConfirmed() {
        return confirmed;
    }
    public void setConfirmed() {
        this.confirmed = true;
    }
}
