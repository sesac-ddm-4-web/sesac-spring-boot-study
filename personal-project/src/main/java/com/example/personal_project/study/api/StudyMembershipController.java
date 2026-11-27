package com.example.personal_project.study.api;

import com.example.personal_project.common.api.ApiRequest;
import com.example.personal_project.common.api.PageResponse;
import com.example.personal_project.study.application.StudyMembershipService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StudyMembershipController {

    private static final String MEMBERS_PATH = StudyController.PATH + "/{studyId}/members";

    private final StudyMembershipService membershipService;

    public StudyMembershipController(StudyMembershipService membershipService) {
        this.membershipService = membershipService;
    }

    @PostMapping(MEMBERS_PATH)
    @ResponseStatus(HttpStatus.CREATED)
    public StudyMembershipService.Participation join(
            @PathVariable("studyId") long studyId,
            @RequestHeader(name = ApiRequest.MEMBER_HEADER, required = false) String actor) {
        return membershipService.join(ApiRequest.positiveId(studyId, "studyId"), ApiRequest.actorId(actor));
    }

    @DeleteMapping(MEMBERS_PATH + "/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void leave(
            @PathVariable("studyId") long studyId,
            @RequestHeader(name = ApiRequest.MEMBER_HEADER, required = false) String actor) {
        membershipService.leave(ApiRequest.positiveId(studyId, "studyId"), ApiRequest.actorId(actor));
    }

    @GetMapping(MEMBERS_PATH)
    public PageResponse<StudyMembershipService.Participation> participants(
            @PathVariable("studyId") long studyId,
            @RequestHeader(name = ApiRequest.MEMBER_HEADER, required = false) String actor,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        return PageResponse.from(membershipService.participants(ApiRequest.positiveId(studyId, "studyId"),
                ApiRequest.actorId(actor), ApiRequest.page(page, size)));
    }

    @GetMapping("/api/v1/members/me/studies")
    public PageResponse<StudyMembershipService.MyStudy> myStudies(
            @RequestHeader(name = ApiRequest.MEMBER_HEADER, required = false) String actor,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        return PageResponse.from(membershipService.myStudies(ApiRequest.actorId(actor), ApiRequest.page(page, size)));
    }
}
