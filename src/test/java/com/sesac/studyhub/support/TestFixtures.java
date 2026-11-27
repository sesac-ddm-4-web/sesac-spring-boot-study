package com.sesac.studyhub.support;

import com.sesac.studyhub.global.exception.BusinessException;
import com.sesac.studyhub.global.exception.ErrorCode;
import com.sesac.studyhub.member.domain.Member;
import com.sesac.studyhub.study.domain.Study;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 단위 테스트용 객체 생성 도우미.
 * DB 없이 id를 직접 넣어야 할 때 ReflectionTestUtils로 BaseEntity의 id를 채운다.
 */
public final class TestFixtures {

    private TestFixtures() {
    }

    public static Member member(Long id, String name) {
        Member member = Member.create(name + "@test.com", "encoded-password", name);
        ReflectionTestUtils.setField(member, "id", id);
        return member;
    }

    public static Study study(Long id, Member leader, int capacity) {
        Study study = Study.create("테스트 스터디", "설명", capacity, leader);
        ReflectionTestUtils.setField(study, "id", id);
        return study;
    }

    /** 실행했을 때 특정 ErrorCode의 BusinessException이 나는지 검증 */
    public static void assertBusinessError(ThrowingCallable callable, ErrorCode expected) {
        assertThatThrownBy(callable)
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(expected);
    }
}