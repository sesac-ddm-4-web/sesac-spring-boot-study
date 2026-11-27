package com.example.study.domain;

import java.util.Locale;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import org.hibernate.annotations.Collate;

@Entity
@Table(name = "member", uniqueConstraints = {
        @UniqueConstraint(name = Member.EMAIL_UNIQUE_CONSTRAINT, columnNames = "email"),
        @UniqueConstraint(name = "uk_member_profile", columnNames = "profile_id")
})
public class Member {

    public static final String EMAIL_UNIQUE_CONSTRAINT = "uk_member_email";
    public static final int EMAIL_MAX_LENGTH = 100;
    public static final int NAME_MAX_LENGTH = 30;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 이메일은 소문자로 정규화해 저장하므로 글자 그대로 비교한다.
    // 기본 collation은 악센트(é와 e)를 구분하지 않는다.
    @Collate("utf8mb4_bin")
    @Column(nullable = false, length = EMAIL_MAX_LENGTH)
    private String email;

    /** BCrypt 해시. 원문은 저장하지 않는다. */
    @Column(nullable = false, length = 60)
    private String password;

    @Column(nullable = false, length = NAME_MAX_LENGTH)
    private String name;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, optional = false)
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile profile;

    protected Member() {
    }

    public Member(String email, String encodedPassword, String name, String introduction) {
        this.email = normalizeEmail(email);
        this.password = encodedPassword;
        this.name = name;
        this.profile = new Profile(introduction);
    }

    /** 가입과 로그인에서 같은 기준으로 이메일을 비교하기 위한 규칙. */
    public static String normalizeEmail(String email) {
        return email == null ? null : email.strip().toLowerCase(Locale.ROOT);
    }

    public void changeName(String name) {
        this.name = name;
    }

    public void changeIntroduction(String introduction) {
        profile.changeIntroduction(introduction);
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public Profile getProfile() {
        return profile;
    }
}
