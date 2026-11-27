package com.example.study.controller;

import java.util.List;

import com.example.study.common.auth.LoginMember;
import com.example.study.dto.CreateStudyRequest;
import com.example.study.dto.RecruitStatusRequest;
import com.example.study.dto.StudyDetailResponse;
import com.example.study.dto.StudySummaryResponse;
import com.example.study.service.StudyDeletionService;
import com.example.study.service.StudyService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/studies")
public class StudyController {

    private final StudyService studyService;
    private final StudyDeletionService studyDeletionService;

    public StudyController(StudyService studyService, StudyDeletionService studyDeletionService) {
        this.studyService = studyService;
        this.studyDeletionService = studyDeletionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StudyDetailResponse open(@LoginMember Long memberId,
            @Valid @RequestBody CreateStudyRequest request) {
        return studyService.open(memberId, request);
    }

    @GetMapping
    public List<StudySummaryResponse> getStudies() {
        return studyService.getStudies();
    }

    @GetMapping("/{studyId}")
    public StudyDetailResponse getStudy(@LoginMember Long memberId, @PathVariable("studyId") Long studyId) {
        return studyService.getStudy(memberId, studyId);
    }

    @PatchMapping("/{studyId}/recruit-status")
    public StudyDetailResponse changeRecruitStatus(@LoginMember Long memberId,
            @PathVariable("studyId") Long studyId, @Valid @RequestBody RecruitStatusRequest request) {
        return studyService.changeRecruitStatus(memberId, studyId, request);
    }

    @DeleteMapping("/{studyId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@LoginMember Long memberId, @PathVariable("studyId") Long studyId) {
        studyDeletionService.delete(memberId, studyId);
    }
}
