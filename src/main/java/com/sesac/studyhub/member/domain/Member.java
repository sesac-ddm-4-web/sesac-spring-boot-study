package com.sesac.studyhub.member.domain;

import com.sesac.studyhub.global.common.BaseEntity;
import jakarta.persistence.*;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 30)
    private String name;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "profile_id", unique = true)
    private Profile profile;

    private Member(String email, String password, String name) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.profile = Profile.empty();
    }

    public static Member create(String email, String encodedPassword, String name) {
        return new Member(email, encodedPassword, name);
    }

    public void updateProfile(String imageUrl, String description) {
        this.profile.update(imageUrl, description);
    }

}
