package com.example.sesac_spring_boot_study.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "study_groups")
public class StudyGroup {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "leader_id", nullable = false)
    private Member leader;
    @Column(nullable = false, length = 120)
    private String title;
    @Column(nullable = false, length = 1000)
    private String description;
    @Column(nullable = false)
    private int capacity;

    protected StudyGroup() {}

    public StudyGroup(Member leader, String title, String description, int capacity) {
        this.leader = leader;
        this.title = title;
        this.description = description;
        this.capacity = capacity;
    }

    public Long getId() {
        return id;
    }

    public Member getLeader() {
        return leader;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getCapacity() {
        return capacity;
    }
}
