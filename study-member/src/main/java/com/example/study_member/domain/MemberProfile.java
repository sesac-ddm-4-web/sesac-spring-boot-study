package com.example.study_member.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.Objects;

@Entity
public class MemberProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "member_id", nullable = false, unique = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
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

    public Long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public void updateDescription(String description){
        this.description = description;
    }
}
