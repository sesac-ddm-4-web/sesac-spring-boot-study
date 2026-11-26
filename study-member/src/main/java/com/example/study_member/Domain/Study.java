package com.example.study_member.Domain;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
public class Study {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "creator_id", nullable = false)
    private Member creator;

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(nullable = false)
    private Integer capacity;

    protected Study() {
    }

    public Study(Member creator, String title, String description, int capacity) {
        if (capacity < 1) {
            throw new IllegalArgumentException("스터디 정원은 1명 이상이어야 합니다.");
        }
        this.creator = Objects.requireNonNull(creator, "creator");
        this.title = Objects.requireNonNull(title, "title");
        this.description = description;
        this.capacity = capacity;
    }

    public Member getCreator() {
        return creator;
    }

    public void setCreator(Member creator) {
        this.creator = Objects.requireNonNull(creator, "creator");
    }
}
