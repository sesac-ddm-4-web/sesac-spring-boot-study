package com.example.studyservice.study.service;

import com.example.studyservice.global.exception.ApiException;
import com.example.studyservice.global.exception.ErrorCode;
import com.example.studyservice.member.domain.Member;
import com.example.studyservice.member.repository.MemberRepository;
import com.example.studyservice.study.domain.ParticipationStatus;
import com.example.studyservice.study.domain.Study;
import com.example.studyservice.study.domain.StudyMember;
import com.example.studyservice.study.domain.StudyRole;
import com.example.studyservice.study.domain.StudyStatus;
import com.example.studyservice.study.dto.req.StudyCreateReq;
import com.example.studyservice.study.dto.req.StudyUpdateReq;
import com.example.studyservice.study.dto.res.StudyDetailRes;
import com.example.studyservice.study.dto.res.StudySummaryRes;
import com.example.studyservice.study.repository.StudyMemberRepository;
import com.example.studyservice.study.repository.StudyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class StudyService {

    private final StudyRepository studyRepository;
    private final StudyMemberRepository studyMemberRepository;
    private final MemberRepository memberRepository;
    private final StudyReader studyReader;
    private final Clock clock;

    public StudyService(StudyRepository studyRepository, StudyMemberRepository studyMemberRepository,
                        MemberRepository memberRepository, StudyReader studyReader, Clock clock) {
        this.studyRepository = studyRepository;
        this.studyMemberRepository = studyMemberRepository;
        this.memberRepository = memberRepository;
        this.studyReader = studyReader;
        this.clock = clock;
    }

    @Transactional
    public StudyDetailRes create(Long memberId, StudyCreateReq request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ApiException(ErrorCode.MEMBER_NOT_FOUND, memberId));
        LocalDateTime now = LocalDateTime.now(clock);
        Study study = studyRepository.save(new Study(request.title(), request.description(), request.capacity(), now));
        StudyMember leader = studyMemberRepository.save(StudyMember.leader(study, member, now));
        return StudyDetailRes.of(study, leader);
    }

    public List<StudySummaryRes> search(StudyStatus status, String keyword) {
        String normalizedKeyword = keyword == null || keyword.isBlank() ? null : keyword.strip();
        List<Study> studies = studyRepository.search(status, normalizedKeyword);
        if (studies.isEmpty()) {
            return List.of();
        }
        Map<Long, StudyMember> leaders = studyMemberRepository.findByStudyIdInAndRoleAndStatus(
                        studies.stream().map(Study::getId).toList(), StudyRole.LEADER, ParticipationStatus.ACTIVE)
                .stream()
                .collect(Collectors.toMap(leader -> leader.getStudy().getId(), Function.identity()));
        return studies.stream()
                .map(study -> StudySummaryRes.of(study, leaders.get(study.getId())))
                .toList();
    }

    public StudyDetailRes findDetail(Long studyId) {
        Study study = studyReader.findStudy(studyId);
        return StudyDetailRes.of(study, findLeader(studyId));
    }

    @Transactional
    public StudyDetailRes update(Long studyId, Long memberId, StudyUpdateReq request) {
        Study study = studyReader.findStudyForUpdate(studyId);
        StudyMember leader = studyReader.requireLeader(studyId, memberId);
        study.update(request.title(), request.description(), request.capacity());
        return StudyDetailRes.of(study, leader);
    }

    private StudyMember findLeader(Long studyId) {
        return studyMemberRepository.findByStudyIdInAndRoleAndStatus(
                        List.of(studyId), StudyRole.LEADER, ParticipationStatus.ACTIVE)
                .stream()
                .findFirst()
                .orElse(null);
    }
}
