package com.example.personal_project.member.domain;

import com.example.personal_project.common.DomainText;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

@Entity
@Table(name = "member_profiles")
public class MemberProfile {

    private static final int NICKNAME_MIN_LENGTH = 2;
    private static final int NICKNAME_MAX_LENGTH = 30;
    private static final int BIO_MAX_LENGTH = 500;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false, unique = true, updatable = false)
    private Member member;

    @Column(nullable = false, length = NICKNAME_MAX_LENGTH)
    private String nickname;

    @Column(length = BIO_MAX_LENGTH)
    private String bio;

    @Column(name = "updated_at", nullable = false, secondPrecision = 6)
    private Instant updatedAt;

    protected MemberProfile() {
    }

    private MemberProfile(Member member, String nickname, String bio, Instant now) {
        this.member = Objects.requireNonNull(member, "member");
        update(nickname, bio, now);
    }

    public static MemberProfile create(Member member, String nickname, String bio, Instant now) {
        return new MemberProfile(member, nickname, bio, now);
    }

    public void update(String nickname, String bio, Instant now) {
        String updatedNickname = DomainText.requireLength(nickname, "nickname", NICKNAME_MIN_LENGTH, NICKNAME_MAX_LENGTH);
        String updatedBio = bio == null || bio.isBlank() ? null : DomainText.requireLength(bio, "bio", 1, BIO_MAX_LENGTH);
        Instant updatedTime = Objects.requireNonNull(now, "now").truncatedTo(ChronoUnit.MICROS);

        this.nickname = updatedNickname;
        this.bio = updatedBio;
        this.updatedAt = updatedTime;
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

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
