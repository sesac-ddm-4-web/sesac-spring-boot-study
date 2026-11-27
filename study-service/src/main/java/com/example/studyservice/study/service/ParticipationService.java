package com.example.studyservice.study.service;

import com.example.studyservice.global.exception.ApiException;
import com.example.studyservice.global.exception.ErrorCode;
import com.example.studyservice.member.domain.Member;
import com.example.studyservice.member.repository.MemberRepository;
import com.example.studyservice.study.domain.ParticipationStatus;
import com.example.studyservice.study.domain.Study;
import com.example.studyservice.study.domain.StudyMember;
import com.example.studyservice.study.dto.res.MyStudyRes;
import com.example.studyservice.study.dto.res.StudyMemberRes;
import com.example.studyservice.study.repository.StudyMemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class ParticipationService {

    private final StudyMemberRepository studyMemberRepository;
    private final MemberRepository memberRepository;
    private final StudyReader studyReader;
    private final Clock clock;

    public ParticipationService(StudyMemberRepository studyMemberRepository, MemberRepository memberRepository,
                                StudyReader studyReader, Clock clock) {
        this.studyMemberRepository = studyMemberRepository;
        this.memberRepository = memberRepository;
        this.studyReader = studyReader;
        this.clock = clock;
    }

    @Transactional
    public StudyMemberRes join(Long studyId, Long memberId) {
        Study study = studyReader.findStudyForUpdate(studyId);
        Optional<StudyMember> history = studyMemberRepository.findByStudyIdAndMemberId(studyId, memberId);
        if (history.isPresent() && history.get().isActive()) {
            throw new ApiException(ErrorCode.ALREADY_JOINED);
        }
        study.increaseMemberCount();

        LocalDateTime now = LocalDateTime.now(clock);
        if (history.isPresent()) {
            StudyMember rejoined = history.get();
            rejoined.rejoin(now);
            return StudyMemberRes.of(rejoined);
        }
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ApiException(ErrorCode.MEMBER_NOT_FOUND, memberId));
        return StudyMemberRes.of(studyMemberRepository.save(StudyMember.member(study, member, now)));
    }

    @Transactional
    public void leave(Long studyId, Long memberId) {
        Study study = studyReader.findStudyForUpdate(studyId);
        StudyMember me = studyReader.requireActiveMember(studyId, memberId);
        if (me.isLeader() && study.getMemberCount() > 1) {
            throw new ApiException(ErrorCode.LEADER_MUST_DELEGATE);
        }

        LocalDateTime now = LocalDateTime.now(clock);
        me.leave(now);
        study.decreaseMemberCount();
        if (me.isLeader()) {
            study.close(now);
        }
    }

    @Transactional
    public StudyMemberRes delegateLeader(Long studyId, Long leaderId, Long nextLeaderId) {
        Study study = studyReader.findStudyForUpdate(studyId);
        study.validateActive();
        StudyMember leader = studyReader.requireLeader(studyId, leaderId);
        if (leaderId.equals(nextLeaderId)) {
            throw new ApiException(ErrorCode.CANNOT_DELEGATE_TO_SELF);
        }
        StudyMember next = studyMemberRepository.findByStudyIdAndMemberId(studyId, nextLeaderId)
                .filter(StudyMember::isActive)
                .orElseThrow(() -> new ApiException(ErrorCode.DELEGATE_TARGET_NOT_MEMBER, nextLeaderId));
        leader.handOverLeaderTo(next);
        return StudyMemberRes.of(next);
    }

    public List<StudyMemberRes> findMembers(Long studyId) {
        studyReader.findStudy(studyId);
        return studyMemberRepository.findByStudyIdAndStatus(studyId, ParticipationStatus.ACTIVE).stream()
                .map(StudyMemberRes::of)
                .toList();
    }

    public List<MyStudyRes> findMyStudies(Long memberId) {
        return studyMemberRepository.findByMemberIdAndStatus(memberId, ParticipationStatus.ACTIVE).stream()
                .map(MyStudyRes::of)
                .toList();
    }
}
