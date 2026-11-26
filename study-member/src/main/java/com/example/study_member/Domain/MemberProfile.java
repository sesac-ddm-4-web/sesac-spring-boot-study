package com.example.study_member.Domain;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
public class MemberProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "member_id", nullable = false, unique = true)
    private Member member;

    private String description;

    protected MemberProfile() {
    }

    public MemberProfile(Member member, String description) {
        this.member = Objects.requireNonNull(member, "member");
        this.description = description;
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = Objects.requireNonNull(member, "member");
    }
}
