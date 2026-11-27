package com.sesac.studyhub.study.service;

import com.sesac.studyhub.global.exception.ErrorCode;
import com.sesac.studyhub.member.domain.Member;
import com.sesac.studyhub.member.repository.MemberRepository;
import com.sesac.studyhub.study.domain.Study;
import com.sesac.studyhub.study.domain.StudyMember;
import com.sesac.studyhub.study.dto.StudyJoinResponse;
import com.sesac.studyhub.study.repository.StudyMemberRepository;
import com.sesac.studyhub.study.repository.StudyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.sesac.studyhub.support.TestFixtures.assertBusinessError;
import static com.sesac.studyhub.support.TestFixtures.member;
import static com.sesac.studyhub.support.TestFixtures.study;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class StudyMemberServiceTest {

    private static final Long STUDY_ID = 10L;
    private static final Long LEADER_ID = 1L;
    private static final Long APPLICANT_ID = 2L;

    @Mock StudyRepository studyRepository;
    @Mock StudyMemberRepository studyMemberRepository;
    @Mock MemberRepository memberRepository;
    @InjectMocks StudyMemberService studyMemberService;

    private Member leader;
    private Member applicant;
    private Study study;

    @BeforeEach
    void setUp() {
        leader = member(LEADER_ID, "leader");
        applicant = member(APPLICANT_ID, "applicant");
        study = study(STUDY_ID, leader, 3);
    }

    @Nested
    @DisplayName("스터디 참여")
    class Join {

        @BeforeEach
        void stubStudyAndMember() {
            given(studyRepository.findByIdForUpdate(STUDY_ID)).willReturn(Optional.of(study));
            given(memberRepository.findById(APPLICANT_ID)).willReturn(Optional.of(applicant));
        }

        @Test
        @DisplayName("정원이 남아 있으면 참여하고 현재 인원을 돌려준다")
        void success() {
            given(studyMemberRepository.existsByStudyIdAndMemberId(STUDY_ID, APPLICANT_ID)).willReturn(false);
            given(studyMemberRepository.countByStudyId(STUDY_ID)).willReturn(2L);

            StudyJoinResponse response = studyMemberService.join(STUDY_ID, APPLICANT_ID);

            assertThat(response.currentMemberCount()).isEqualTo(3);
            then(studyMemberRepository).should().save(any(StudyMember.class));
        }

        @Test
        @DisplayName("모집 마감된 스터디면 STUDY_CLOSED")
        void closed() {
            study.close();

            assertBusinessError(() -> studyMemberService.join(STUDY_ID, APPLICANT_ID), ErrorCode.STUDY_CLOSED);
            then(studyMemberRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("이미 참여한 스터디면 ALREADY_JOINED")
        void alreadyJoined() {
            given(studyMemberRepository.existsByStudyIdAndMemberId(STUDY_ID, APPLICANT_ID)).willReturn(true);

            assertBusinessError(() -> studyMemberService.join(STUDY_ID, APPLICANT_ID), ErrorCode.ALREADY_JOINED);
            then(studyMemberRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("정원이 가득 찼으면 STUDY_FULL")
        void full() {
            given(studyMemberRepository.existsByStudyIdAndMemberId(STUDY_ID, APPLICANT_ID)).willReturn(false);
            given(studyMemberRepository.countByStudyId(STUDY_ID)).willReturn(3L);

            assertBusinessError(() -> studyMemberService.join(STUDY_ID, APPLICANT_ID), ErrorCode.STUDY_FULL);
            then(studyMemberRepository).should(never()).save(any());
        }
    }

    @Test
    @DisplayName("없는 스터디에 참여하면 STUDY_NOT_FOUND")
    void joinNotFound() {
        given(studyRepository.findByIdForUpdate(999L)).willReturn(Optional.empty());

        assertBusinessError(() -> studyMemberService.join(999L, APPLICANT_ID), ErrorCode.STUDY_NOT_FOUND);
    }

    @Nested
    @DisplayName("스터디 탈퇴")
    class Leave {

        @BeforeEach
        void stubStudyExists() {
            given(studyRepository.existsById(STUDY_ID)).willReturn(true);
        }

        @Test
        @DisplayName("일반 멤버는 탈퇴하면 참여 정보가 삭제된다")
        void success() {
            StudyMember studyMember = StudyMember.member(study, applicant);
            given(studyMemberRepository.findByStudyIdAndMemberId(STUDY_ID, APPLICANT_ID))
                .willReturn(Optional.of(studyMember));

            studyMemberService.leave(STUDY_ID, APPLICANT_ID);

            then(studyMemberRepository).should().delete(studyMember);
        }

        @Test
        @DisplayName("스터디장은 탈퇴할 수 없다 (LEADER_CANNOT_LEAVE)")
        void leaderCannotLeave() {
            given(studyMemberRepository.findByStudyIdAndMemberId(STUDY_ID, LEADER_ID))
                .willReturn(Optional.of(StudyMember.leader(study, leader)));

            assertBusinessError(() -> studyMemberService.leave(STUDY_ID, LEADER_ID), ErrorCode.LEADER_CANNOT_LEAVE);
            then(studyMemberRepository).should(never()).delete(any());
        }

        @Test
        @DisplayName("참여하지 않은 스터디면 NOT_STUDY_MEMBER")
        void notMember() {
            given(studyMemberRepository.findByStudyIdAndMemberId(STUDY_ID, APPLICANT_ID))
                .willReturn(Optional.empty());

            assertBusinessError(() -> studyMemberService.leave(STUDY_ID, APPLICANT_ID), ErrorCode.NOT_STUDY_MEMBER);
        }
    }
}