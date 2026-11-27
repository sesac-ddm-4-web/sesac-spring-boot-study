package com.example.sesac_spring_boot_study.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "member_profiles")
public class Profile {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false, unique = true)
    private Member member;
    @Column(nullable = false, length = 1000)
    private String bio;

    protected Profile() {}

    public Profile(Member member, String bio) {
        this.member = member;
        this.bio = bio;
    }

    public Member getMember() {
        return member;
    }

    public String getBio() {
        return bio;
    }

    public void updateBio(String bio) {
        this.bio = bio;
    }
}
