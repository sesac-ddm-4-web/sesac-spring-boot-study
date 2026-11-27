package com.sesac.studyhub.member.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MemberTest {

    @Test
    @DisplayName("회원을 만들면 빈 프로필이 함께 만들어진다")
    void createWithEmptyProfile() {
        Member member = Member.create("a@test.com", "encoded", "서연");

        assertThat(member.getProfile()).isNotNull();
        assertThat(member.getProfile().getImageUrl()).isNull();
        assertThat(member.getProfile().getDescription()).isEmpty();
    }

    @Test
    @DisplayName("프로필 수정 시 null인 필드는 기존 값을 유지한다 (부분 수정)")
    void partialUpdate() {
        Member member = Member.create("a@test.com", "encoded", "서연");
        member.updateProfile("https://img.com/a.png", "자기소개");

        member.updateProfile(null, "바뀐 자기소개");

        assertThat(member.getProfile().getImageUrl()).isEqualTo("https://img.com/a.png");
        assertThat(member.getProfile().getDescription()).isEqualTo("바뀐 자기소개");
    }

    @Test
    @DisplayName("이미지 URL에 빈 문자열을 보내면 이미지가 삭제된다")
    void deleteImage() {
        Member member = Member.create("a@test.com", "encoded", "서연");
        member.updateProfile("https://img.com/a.png", null);

        member.updateProfile("", null);

        assertThat(member.getProfile().getImageUrl()).isNull();
    }
}