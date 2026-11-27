package com.example.study.service;

import com.example.study.common.exception.BusinessException;
import com.example.study.common.exception.ErrorCode;
import com.example.study.domain.Member;
import com.example.study.dto.MemberResponse;
import com.example.study.dto.SignupRequest;
import com.example.study.dto.UpdateMemberRequest;
import com.example.study.repository.MemberRepository;

import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@Transactional(readOnly = true)
public class MemberService {

    private static final Logger log = LoggerFactory.getLogger(MemberService.class);

    private static final int MAX_SIGN_UP_ATTEMPTS = 3;

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final TransactionTemplate transaction;

    public MemberService(MemberRepository memberRepository, PasswordEncoder passwordEncoder,
            PlatformTransactionManager transactionManager) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.transaction = new TransactionTemplate(transactionManager);
    }

    /**
     * 같은 이메일을 동시에 INSERT하면 중복 키 잠금끼리 데드락이 날 수 있다.
     * 데드락으로 롤백된 요청은 새 트랜잭션에서 다시 시도해 중복 여부를 확정한다.
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public MemberResponse signUp(SignupRequest request) {
        // 해시 계산은 느리므로 트랜잭션 밖에서 한 번만 한다.
        String encodedPassword = passwordEncoder.encode(request.password());
        for (int attempt = 0; attempt < MAX_SIGN_UP_ATTEMPTS; attempt++) {
            try {
                return transaction.execute(status -> register(request, encodedPassword));
            } catch (PessimisticLockingFailureException e) {
                log.info("가입 중 잠금 충돌, 다시 시도합니다.");
            }
        }
        throw new BusinessException(ErrorCode.CONCURRENT_MODIFICATION);
    }

    private MemberResponse register(SignupRequest request, String encodedPassword) {
        if (memberRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }
        Member member = new Member(
                request.email(),
                encodedPassword,
                request.name(),
                request.introduction());
        try {
            // 동시에 가입하면 위 검사를 함께 통과할 수 있다. 최종 판정은 DB unique 제약이 한다.
            memberRepository.saveAndFlush(member);
        } catch (DataIntegrityViolationException e) {
            if (isDuplicateEmail(e)) {
                throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
            }
            throw e;
        }
        return MemberResponse.from(member);
    }

    public MemberResponse getMember(Long memberId) {
        return MemberResponse.from(findMember(memberId));
    }

    @Transactional
    public MemberResponse updateMember(Long memberId, UpdateMemberRequest request) {
        if (request.name() == null && request.introduction() == null) {
            throw new BusinessException(ErrorCode.NOTHING_TO_UPDATE);
        }
        Member member = findMember(memberId);
        if (request.name() != null) {
            member.changeName(request.name());
        }
        if (request.introduction() != null) {
            member.changeIntroduction(request.introduction());
        }
        // 변경 감지로 커밋할 때 반영된다.
        return MemberResponse.from(member);
    }

    private Member findMember(Long memberId) {
        // 세션의 회원이 DB에 없으면 로그인 상태로 보지 않는다.
        return memberRepository.findWithProfileById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));
    }

    private boolean isDuplicateEmail(DataIntegrityViolationException e) {
        return e.getCause() instanceof ConstraintViolationException violation
                && violation.getConstraintName() != null
                && violation.getConstraintName().endsWith(Member.EMAIL_UNIQUE_CONSTRAINT);
    }
}
