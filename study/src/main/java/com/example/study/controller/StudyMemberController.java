package com.example.study.controller;

import com.example.study.common.auth.LoginMember;
import com.example.study.dto.StudyDetailResponse;
import com.example.study.service.StudyParticipationService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/studies/{studyId}/members")
public class StudyMemberController {

    private final StudyParticipationService participationService;

    public StudyMemberController(StudyParticipationService participationService) {
        this.participationService = participationService;
    }

    /** 참여와 재가입 */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StudyDetailResponse join(@LoginMember Long memberId, @PathVariable("studyId") Long studyId) {
        return participationService.join(memberId, studyId);
    }

    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void leave(@LoginMember Long memberId, @PathVariable("studyId") Long studyId) {
        participationService.leave(memberId, studyId);
    }
}
