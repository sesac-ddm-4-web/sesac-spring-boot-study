package com.example.personal_project.study.application;

import com.example.personal_project.common.application.ServiceException;
import com.example.personal_project.member.application.MemberService;
import com.example.personal_project.member.domain.Member;
import com.example.personal_project.study.domain.MembershipRole;
import com.example.personal_project.study.domain.MembershipStatus;
import com.example.personal_project.study.domain.RecruitmentStatus;
import com.example.personal_project.study.domain.Study;
import com.example.personal_project.study.domain.StudyMembership;
import com.example.personal_project.study.repository.StudyMembershipRepository;
import com.example.personal_project.study.repository.StudyRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class StudyService {

    private static final Sort NEWEST_FIRST = Sort.by(Sort.Direction.DESC, "createdAt", "id");

    private final StudyRepository studies;
    private final StudyMembershipRepository memberships;
    private final MemberService members;

    public StudyService(StudyRepository studies, StudyMembershipRepository memberships, MemberService members) {
        this.studies = studies;
        this.memberships = memberships;
        this.members = members;
    }

    @Transactional
    public StudyView.Detail create(long actorId, String title, String description, int capacity) {
        Member owner = members.requireMember(actorId);
        Instant now = Instant.now();
        Study study = studies.save(Study.open(title, description, capacity, now));
        memberships.save(StudyMembership.owner(study, owner, now));
        return StudyView.Detail.from(study, StudyView.Summary.from(study, actorId, 1));
    }

    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public Page<StudyView.Summary> list(RecruitmentStatus status, Pageable page) {
        Pageable sorted = PageRequest.of(page.getPageNumber(), page.getPageSize(), NEWEST_FIRST);
        Page<Study> result = status == null ? studies.findAll(sorted) : studies.findByRecruitmentStatus(status, sorted);
        Map<Long, StudyView.Summary> summaries = summariesFor(result.getContent());
        return result.map(study -> summaries.get(study.getId()));
    }

    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public StudyView.Detail get(long studyId) {
        return detail(requireStudy(studyId));
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public StudyView.Detail changeRecruitment(long studyId, long actorId, RecruitmentStatus status) {
        members.requireMember(actorId);
        Study study = lockStudy(studyId);
        requireOwner(studyId, actorId);
        study.changeRecruitmentStatus(status);
        return detail(study);
    }

    Study requireStudy(long studyId) {
        return studies.findById(studyId).orElseThrow(() -> new ServiceException(ServiceException.Code.STUDY_NOT_FOUND));
    }

    Study lockStudy(long studyId) {
        return studies.findByIdForUpdate(studyId).orElseThrow(() -> new ServiceException(ServiceException.Code.STUDY_NOT_FOUND));
    }

    void requireOwner(long studyId, long actorId) {
        memberships.findByStudy_IdAndMember_Id(studyId, actorId)
                .filter(membership -> membership.getRole() == MembershipRole.OWNER && membership.getStatus() == MembershipStatus.ACTIVE)
                .orElseThrow(() -> new ServiceException(ServiceException.Code.OWNER_REQUIRED));
    }

    Map<Long, StudyView.Summary> summariesFor(List<Study> source) {
        if (source.isEmpty()) {
            return Map.of();
        }
        List<Long> ids = source.stream().map(Study::getId).toList();
        Map<Long, StudyMembershipRepository.ActiveStats> statsByStudy = memberships
                .findActiveStats(ids, MembershipStatus.ACTIVE, MembershipRole.OWNER).stream()
                .collect(Collectors.toMap(StudyMembershipRepository.ActiveStats::getStudyId, Function.identity()));
        return source.stream().collect(Collectors.toMap(Study::getId, study -> {
            StudyMembershipRepository.ActiveStats stats = statsByStudy.get(study.getId());
            if (stats == null || stats.getOwnerMemberId() == null) {
                throw new IllegalStateException("Study " + study.getId() + " has no active owner");
            }
            return StudyView.Summary.from(study, stats.getOwnerMemberId(), stats.getActiveMemberCount());
        }));
    }

    private StudyView.Detail detail(Study study) {
        return StudyView.Detail.from(study, summariesFor(List.of(study)).get(study.getId()));
    }
}
