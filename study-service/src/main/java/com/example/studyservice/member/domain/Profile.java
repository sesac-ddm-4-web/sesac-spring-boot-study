package com.example.studyservice.member.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "profile")
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false, unique = true)
    private Member member;

    @Column(nullable = false, length = 30)
    private String nickname;

    @Column(length = 500)
    private String bio;

    @Column(length = 200)
    private String interests;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected Profile() {
    }

    public Profile(Member member, String nickname, LocalDateTime now) {
        this.member = Objects.requireNonNull(member);
        this.nickname = Objects.requireNonNull(nickname).strip();
        this.updatedAt = Objects.requireNonNull(now);
    }

    public void update(String nickname, String bio, String interests, LocalDateTime now) {
        this.nickname = Objects.requireNonNull(nickname).strip();
        this.bio = blankToNull(bio);
        this.interests = blankToNull(interests);
        this.updatedAt = now;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }

    public Long getId() {
        return id;
    }

    public Member getMember() {
        return member;
    }

    public String getNickname() {
        return nickname;
    }

    public String getBio() {
        return bio;
    }

    public String getInterests() {
        return interests;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
