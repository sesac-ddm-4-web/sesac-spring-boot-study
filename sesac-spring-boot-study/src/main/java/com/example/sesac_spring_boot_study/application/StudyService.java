package com.example.sesac_spring_boot_study.application;

import com.example.sesac_spring_boot_study.domain.Member;
import com.example.sesac_spring_boot_study.domain.Study;
import com.example.sesac_spring_boot_study.domain.StudyMember;
import com.example.sesac_spring_boot_study.exception.BusinessException;
import com.example.sesac_spring_boot_study.exception.ErrorCode;
import com.example.sesac_spring_boot_study.infarstructrue.StudyMemberRepository;
import com.example.sesac_spring_boot_study.infarstructrue.StudyRepository;
import com.example.sesac_spring_boot_study.presentation.StudyDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class StudyService {
    private final StudyRepository studyRepository;
    private final StudyMemberRepository studyMemberRepository;
    private final MemberService memberService;

    public StudyService(StudyRepository studyRepository,
                        StudyMemberRepository studyMemberRepository,
                        MemberService memberService) {
        this.studyRepository = studyRepository;
        this.studyMemberRepository = studyMemberRepository;
        this.memberService = memberService;
    }

    @Transactional
    public StudyDto.Detail create(Long leaderId, String title, String description, int maxCapacity) {
        Member leader = memberService.findMember(leaderId);
        Study study = studyRepository.save(new Study(leader, title, description, maxCapacity));
        StudyMember leaderJoin = studyMemberRepository.save(
                new StudyMember(leader, study, LocalDateTime.now()));
        return StudyDto.Detail.of(study, List.of(leaderJoin));
    }

    public List<StudyDto.Summary> findAll() {
        return studyRepository.findAllByOrderByIdDesc().stream()
                .map(StudyDto.Summary::from)
                .toList();
    }

    public StudyDto.Detail findDetail(Long studyId) {
        Study study = studyRepository.findWithLeaderById(studyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "스터디를 찾을 수 없습니다."));
        List<StudyMember> members = studyMemberRepository.findByStudyIdOrderByJoinedAtAsc(studyId);
        return StudyDto.Detail.of(study, members);
    }

    @Transactional
    public StudyDto.Participant join(Long studyId, Long memberId) {
        Study study = studyRepository.findByIdForUpdate(studyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "스터디를 찾을 수 없습니다."));
        Member member = memberService.findMember(memberId);

        if (studyMemberRepository.existsByMemberIdAndStudyId(memberId, studyId)) {
            throw new BusinessException(ErrorCode.CONFLICT, "이미 참여한 스터디입니다.");
        }
        study.validateJoinable(studyMemberRepository.countByStudyId(studyId));

        StudyMember joined = studyMemberRepository.save(
                new StudyMember(member, study, LocalDateTime.now()));
        return StudyDto.Participant.from(joined);
    }

    @Transactional
    public void leave(Long studyId, Long memberId) {
        Study study = findStudy(studyId);
        if (study.isLeader(memberId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "스터디 개설자는 탈퇴할 수 없습니다.");
        }
        StudyMember studyMember = studyMemberRepository.findByMemberIdAndStudyId(memberId, studyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BAD_REQUEST, "참여하지 않은 스터디입니다."));
        studyMemberRepository.delete(studyMember);
    }

    public List<StudyDto.JoinedStudy> findJoinedStudies(Long memberId) {
        memberService.findMember(memberId);
        return studyMemberRepository.findByMemberIdOrderByJoinedAtDesc(memberId).stream()
                .map(StudyDto.JoinedStudy::from)
                .toList();
    }

    public Study findStudy(Long studyId) {
        return studyRepository.findById(studyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "스터디를 찾을 수 없습니다."));

    }
}
