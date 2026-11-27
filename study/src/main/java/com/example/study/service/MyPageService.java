package com.example.study.service;

import java.util.List;

import com.example.study.dto.HistoryResponse;
import com.example.study.dto.MyPageResponse;
import com.example.study.dto.MyStudiesResponse;
import com.example.study.repository.StudyHistoryRepository;
import com.example.study.repository.StudyRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 내 정보 화면의 조회. 하나의 읽기 트랜잭션(REPEATABLE_READ)에서 읽어
 * 목록과 이력이 같은 시점의 데이터가 되게 한다.
 */
@Service
@Transactional(readOnly = true)
public class MyPageService {

    private final MemberService memberService;
    private final StudyRepository studyRepository;
    private final StudyHistoryRepository studyHistoryRepository;

    public MyPageService(MemberService memberService, StudyRepository studyRepository,
            StudyHistoryRepository studyHistoryRepository) {
        this.memberService = memberService;
        this.studyRepository = studyRepository;
        this.studyHistoryRepository = studyHistoryRepository;
    }

    public MyPageResponse getMyPage(Long memberId) {
        List<HistoryResponse> histories = studyHistoryRepository
                .findByMemberIdOrderByEndedAtDescIdDesc(memberId).stream()
                .map(HistoryResponse::from)
                .toList();
        return MyPageResponse.of(memberService.getMember(memberId), getMyStudies(memberId), histories);
    }

    public MyStudiesResponse getMyStudies(Long memberId) {
        return new MyStudiesResponse(
                studyRepository.findOwnedSummaries(memberId),
                studyRepository.findJoinedSummaries(memberId));
    }
}
