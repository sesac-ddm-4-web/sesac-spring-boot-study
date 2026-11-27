package com.sesac.studyhub.study.domain;

import com.sesac.studyhub.member.domain.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.sesac.studyhub.support.TestFixtures.member;
import static com.sesac.studyhub.support.TestFixtures.study;
import static org.assertj.core.api.Assertions.assertThat;

class StudyTest {

    private final Member leader = member(1L, "leader");

    @Test
    @DisplayName("스터디는 모집중 상태로 만들어진다")
    void createRecruiting() {
        Study study = study(10L, leader, 5);

        assertThat(study.getStatus()).isEqualTo(StudyStatus.RECRUITING);
        assertThat(study.isRecruiting()).isTrue();
    }

    @Test
    @DisplayName("스터디장 id로만 isLeader가 true다")
    void isLeader() {
        Study study = study(10L, leader, 5);

        assertThat(study.isLeader(1L)).isTrue();
        assertThat(study.isLeader(2L)).isFalse();
    }

    @Test
    @DisplayName("마감하면 모집중이 아니다")
    void close() {
        Study study = study(10L, leader, 5);

        study.close();

        assertThat(study.getStatus()).isEqualTo(StudyStatus.CLOSED);
        assertThat(study.isRecruiting()).isFalse();
    }
}