package com.example.sesac_spring_boot_study.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "profiles")
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", unique = true, nullable = false)
    private Member member;

    @Column(nullable = false, length = 500)
    private String introduction;

    protected Profile() {
    }

    public Profile(Member member, String introduction) {
        this.member = member;
        this.introduction = introduction;
    }

    public void updateIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public Long getId() { return id; }
    public Member getMember() { return member; }
    public String getIntroduction() { return introduction; }
}
