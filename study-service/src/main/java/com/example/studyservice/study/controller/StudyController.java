package com.example.studyservice.study.controller;

import com.example.studyservice.study.domain.StudyStatus;
import com.example.studyservice.study.dto.req.LeaderDelegateReq;
import com.example.studyservice.study.dto.req.StudyCreateReq;
import com.example.studyservice.study.dto.req.StudyUpdateReq;
import com.example.studyservice.study.dto.res.StudyDetailRes;
import com.example.studyservice.study.dto.res.StudyMemberRes;
import com.example.studyservice.study.dto.res.StudySummaryRes;
import com.example.studyservice.study.service.ParticipationService;
import com.example.studyservice.study.service.StudyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/studies")
public class StudyController {

    private final StudyService studyService;
    private final ParticipationService participationService;

    public StudyController(StudyService studyService, ParticipationService participationService) {
        this.studyService = studyService;
        this.participationService = participationService;
    }

    @PostMapping
    public ResponseEntity<StudyDetailRes> createStudy(@AuthenticationPrincipal Long memberId,
                                                      @Valid @RequestBody StudyCreateReq request) {
        StudyDetailRes created = studyService.create(memberId, request);
        return ResponseEntity.created(URI.create("/studies/" + created.id())).body(created);
    }

    @GetMapping
    public List<StudySummaryRes> searchStudies(@RequestParam(required = false) StudyStatus status,
                                               @RequestParam(required = false) String keyword) {
        return studyService.search(status, keyword);
    }

    @GetMapping("/{studyId}")
    public StudyDetailRes findStudy(@PathVariable Long studyId) {
        return studyService.findDetail(studyId);
    }

    @PutMapping("/{studyId}")
    public StudyDetailRes updateStudy(@PathVariable Long studyId, @AuthenticationPrincipal Long memberId,
                                      @Valid @RequestBody StudyUpdateReq request) {
        return studyService.update(studyId, memberId, request);
    }

    @PostMapping("/{studyId}/members")
    public ResponseEntity<StudyMemberRes> joinStudy(@PathVariable Long studyId,
                                                    @AuthenticationPrincipal Long memberId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(participationService.join(studyId, memberId));
    }

    @DeleteMapping("/{studyId}/members/me")
    public ResponseEntity<Void> leaveStudy(@PathVariable Long studyId, @AuthenticationPrincipal Long memberId) {
        participationService.leave(studyId, memberId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{studyId}/members")
    public List<StudyMemberRes> findStudyMembers(@PathVariable Long studyId) {
        return participationService.findMembers(studyId);
    }

    @PatchMapping("/{studyId}/leader")
    public StudyMemberRes delegateLeader(@PathVariable Long studyId, @AuthenticationPrincipal Long memberId,
                                         @Valid @RequestBody LeaderDelegateReq request) {
        return participationService.delegateLeader(studyId, memberId, request.memberId());
    }
}
