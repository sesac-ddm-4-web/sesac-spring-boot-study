package com.sesac.studyhub.member.domain;

import com.sesac.studyhub.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Profile extends BaseEntity {

    private String imageUrl;

    @Column(length = 500)
    private String description;

    private Profile(String imageUrl, String description) {
        this.imageUrl = imageUrl;
        this.description = description;
    }

    static Profile empty() {
        return new Profile(null, "");
    }

    void update(String imageUrl, String description) {
        if (imageUrl != null) {
            // "" 보내면 이미지 삭제
            this.imageUrl = imageUrl.isBlank() ? null : imageUrl;
        }
        if (description != null) {
            this.description = description;
        }
    }
}
