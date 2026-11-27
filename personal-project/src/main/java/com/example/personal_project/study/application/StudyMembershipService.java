package com.example.personal_project.study.application;

import com.example.personal_project.common.application.ServiceException;
import com.example.personal_project.member.application.MemberService;
import com.example.personal_project.member.domain.Member;
import com.example.personal_project.study.domain.MembershipRole;
import com.example.personal_project.study.domain.MembershipStatus;
import com.example.personal_project.study.domain.Study;
import com.example.personal_project.study.domain.StudyMembership;
import com.example.personal_project.study.repository.StudyMembershipRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;

@Service
public class StudyMembershipService {

    private final StudyService studies;
    private final StudyMembershipRepository memberships;
    private final MemberService members;

    public StudyMembershipService(StudyService studies, StudyMembershipRepository memberships, MemberService members) {
        this.studies = studies;
        this.memberships = memberships;
        this.members = members;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Participation join(long studyId, long actorId) {
        Member member = members.requireMember(actorId);
        Study study = studies.lockStudy(studyId);

        memberships.findByStudy_IdAndMember_Id(studyId, actorId).ifPresent(existing -> {
            ServiceException.Code code = existing.getStatus() == MembershipStatus.ACTIVE
                    ? ServiceException.Code.ALREADY_JOINED : ServiceException.Code.REJOIN_NOT_ALLOWED;
            throw new ServiceException(code);
        });
        if (!study.isRecruiting()) {
            throw new ServiceException(ServiceException.Code.RECRUITMENT_CLOSED);
        }
        if (memberships.countByStudy_IdAndStatus(studyId, MembershipStatus.ACTIVE) >= study.getCapacity()) {
            throw new ServiceException(ServiceException.Code.STUDY_FULL);
        }

        StudyMembership membership = memberships.save(StudyMembership.join(study, member, Instant.now()));
        return new Participation(membership.getId(), actorId, members.getProfile(actorId).nickname(),
                membership.getRole(), membership.getJoinedAt());
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void leave(long studyId, long actorId) {
        members.requireMember(actorId);
        studies.lockStudy(studyId);
        StudyMembership membership = memberships.findByStudy_IdAndMember_Id(studyId, actorId)
                .orElseThrow(() -> new ServiceException(ServiceException.Code.MEMBERSHIP_NOT_FOUND));
        if (membership.getRole() == MembershipRole.OWNER) {
            throw new ServiceException(ServiceException.Code.OWNER_CANNOT_LEAVE);
        }
        membership.leave(Instant.now());
    }

    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public Page<Participation> participants(long studyId, long actorId, Pageable page) {
        members.requireMember(actorId);
        studies.requireStudy(studyId);
        requireActiveMembership(studyId, actorId);
        return memberships.findParticipants(studyId, MembershipStatus.ACTIVE, page)
                .map(row -> new Participation(row.getMembershipId(), row.getMemberId(), row.getNickname(), row.getRole(), row.getJoinedAt()));
    }

    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public Page<MyStudy> myStudies(long actorId, Pageable page) {
        members.requireMember(actorId);
        Page<StudyMembership> result = memberships.findMemberStudies(actorId, MembershipStatus.ACTIVE, page);
        Map<Long, StudyView.Summary> summaries = studies.summariesFor(result.getContent().stream().map(StudyMembership::getStudy).toList());
        return result.map(membership -> new MyStudy(summaries.get(membership.getStudy().getId()),
                membership.getRole(), membership.getJoinedAt()));
    }

    void requireActiveMembership(long studyId, long actorId) {
        if (!memberships.existsByStudy_IdAndMember_IdAndStatus(studyId, actorId, MembershipStatus.ACTIVE)) {
            throw new ServiceException(ServiceException.Code.ACTIVE_MEMBERSHIP_REQUIRED);
        }
    }

    public record Participation(long membershipId, long memberId, String nickname, MembershipRole role, Instant joinedAt) {
    }

    public record MyStudy(StudyView.Summary study, MembershipRole role, Instant joinedAt) {
    }
}
