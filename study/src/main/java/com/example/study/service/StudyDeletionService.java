package com.example.study.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import com.example.study.common.exception.BusinessException;
import com.example.study.common.exception.ErrorCode;
import com.example.study.domain.Study;
import com.example.study.repository.MemberRepository;
import com.example.study.repository.StudyHistoryRepository;
import com.example.study.repository.StudyMemberRepository;
import com.example.study.repository.StudyRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * 스터디 삭제.
 *
 * <p>이력을 저장하면 FK 검사 때문에 참여자의 회원 행에도 잠금이 걸린다. 스터디를 먼저 잠그면
 * "회원 → 스터디" 순서가 뒤집혀 데드락이 날 수 있으므로, 참여자를 모두 잠근 뒤 스터디를 잠근다.
 * 그 사이에 새 참여자가 생기면 아무것도 바꾸지 않고 처음부터 다시 한다.
 */
@Service
public class StudyDeletionService {

    private static final int MAX_ATTEMPTS = 10;

    private final StudyRepository studyRepository;
    private final StudyMemberRepository studyMemberRepository;
    private final StudyHistoryRepository studyHistoryRepository;
    private final MemberRepository memberRepository;
    private final Clock clock;
    private final TransactionTemplate transaction;

    public StudyDeletionService(StudyRepository studyRepository, StudyMemberRepository studyMemberRepository,
            StudyHistoryRepository studyHistoryRepository, MemberRepository memberRepository, Clock clock,
            PlatformTransactionManager transactionManager) {
        this.studyRepository = studyRepository;
        this.studyMemberRepository = studyMemberRepository;
        this.studyHistoryRepository = studyHistoryRepository;
        this.memberRepository = memberRepository;
        this.clock = clock;
        this.transaction = new TransactionTemplate(transactionManager);
        this.transaction.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
    }

    public void delete(Long requesterId, Long studyId) {
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            // 한 번의 시도가 하나의 트랜잭션이다. 이력 저장과 삭제는 함께 성공하거나 함께 취소된다.
            if (transaction.execute(status -> tryDelete(requesterId, studyId))) {
                return;
            }
        }
        throw new BusinessException(ErrorCode.CONCURRENT_MODIFICATION);
    }

    private boolean tryDelete(Long requesterId, Long studyId) {
        Long ownerId = studyRepository.findOwnerId(studyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDY_NOT_FOUND));
        if (!ownerId.equals(requesterId)) {
            throw new BusinessException(ErrorCode.NOT_STUDY_OWNER);
        }

        // 1. 회원 잠금 (ID 오름차순)
        List<Long> lockedMemberIds = studyMemberRepository.findMemberIds(studyId);
        lockedMemberIds.forEach(memberRepository::findByIdForUpdate);

        // 2. 스터디 잠금
        Study study = studyRepository.findByIdForUpdate(studyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDY_NOT_FOUND));
        study.validateOwner(requesterId);

        // 3. 잠그지 않은 참여자가 생겼는지 확인
        boolean hasUnlockedMember = study.getMembers().stream()
                .anyMatch(studyMember -> !lockedMemberIds.contains(studyMember.getMember().getId()));
        if (hasUnlockedMember) {
            return false;
        }

        studyHistoryRepository.saveAll(study.deletionHistories(LocalDateTime.now(clock)));
        // cascade로 일정과 참여 정보도 함께 삭제된다.
        studyRepository.delete(study);
        return true;
    }
}
