package com.example.personal_project.study.api;

import com.example.personal_project.common.InvalidDomainFieldException;
import com.example.personal_project.common.api.ApiRequest;
import com.example.personal_project.common.api.PageResponse;
import com.example.personal_project.study.application.StudyService;
import com.example.personal_project.study.application.StudyView;
import com.example.personal_project.study.domain.RecruitmentStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping(StudyController.PATH)
public class StudyController {

    static final String PATH = "/api/v1/studies";

    private final StudyService studyService;

    public StudyController(StudyService studyService) {
        this.studyService = studyService;
    }

    @PostMapping
    public ResponseEntity<StudyView.Detail> create(
            @RequestHeader(name = ApiRequest.MEMBER_HEADER, required = false) String actor,
            @RequestBody Map<String, Object> body) {
        StudyView.Detail study = studyService.create(ApiRequest.actorId(actor), ApiRequest.text(body, "title"),
                ApiRequest.text(body, "description"), ApiRequest.integer(body, "capacity"));
        return ResponseEntity.created(URI.create(PATH + "/" + study.studyId())).body(study);
    }

    @GetMapping
    public PageResponse<StudyView.Summary> list(
            @RequestParam(name = "recruitmentStatus", required = false) String status,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        return PageResponse.from(studyService.list(status == null ? null : recruitmentStatus(status), ApiRequest.page(page, size)));
    }

    @GetMapping("/{studyId}")
    public StudyView.Detail get(@PathVariable("studyId") long studyId) {
        return studyService.get(ApiRequest.positiveId(studyId, "studyId"));
    }

    @PutMapping("/{studyId}/recruitment")
    public StudyView.Detail changeRecruitment(
            @PathVariable("studyId") long studyId,
            @RequestHeader(name = ApiRequest.MEMBER_HEADER, required = false) String actor,
            @RequestBody Map<String, Object> body) {
        return studyService.changeRecruitment(ApiRequest.positiveId(studyId, "studyId"), ApiRequest.actorId(actor),
                recruitmentStatus(ApiRequest.text(body, "recruitmentStatus")));
    }

    private static RecruitmentStatus recruitmentStatus(String value) {
        if (RecruitmentStatus.OPEN.name().equals(value)) {
            return RecruitmentStatus.OPEN;
        }
        if (RecruitmentStatus.CLOSED.name().equals(value)) {
            return RecruitmentStatus.CLOSED;
        }
        throw new InvalidDomainFieldException("recruitmentStatus", "recruitmentStatus must be OPEN or CLOSED");
    }
}
