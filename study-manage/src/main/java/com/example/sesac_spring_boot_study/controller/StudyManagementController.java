package com.example.sesac_spring_boot_study.controller;

import java.time.Instant;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.sesac_spring_boot_study.service.StudyManagementService;
import com.example.sesac_spring_boot_study.service.StudyManagementService.MeetingView;
import com.example.sesac_spring_boot_study.service.StudyManagementService.MemberView;
import com.example.sesac_spring_boot_study.service.StudyManagementService.ParticipantView;
import com.example.sesac_spring_boot_study.service.StudyManagementService.StudyView;

@RestController
@RequestMapping("/api")
class StudyManagementController {
    private final StudyManagementService service;

    StudyManagementController(StudyManagementService service) {
        this.service = service;
    }

    @PostMapping("/members")
    @ResponseStatus(HttpStatus.CREATED)
    MemberView createMember(@Valid @RequestBody MemberRequest request) {
        return service.createMember(request.name(), request.bio());
    }

    @GetMapping("/members/{memberId}")
    MemberView getMember(@PathVariable long memberId) {
        return service.getMember(memberId);
    }

    @PutMapping("/members/{memberId}")
    MemberView updateMember(@PathVariable long memberId, @Valid @RequestBody MemberRequest request) {
        return service.updateMember(memberId, request.name(), request.bio());
    }

    @PostMapping("/studies")
    @ResponseStatus(HttpStatus.CREATED)
    StudyView createStudy(@Valid @RequestBody StudyRequest request) {
        return service.createStudy(request.leaderId(), request.title(), request.description(), request.capacity());
    }

    @GetMapping("/studies")
    List<StudyView> listStudies() {
        return service.listStudies();
    }

    @GetMapping("/studies/{studyId}")
    StudyView getStudy(@PathVariable long studyId) {
        return service.getStudy(studyId);
    }

    @PostMapping("/studies/{studyId}/participants")
    @ResponseStatus(HttpStatus.CREATED)
    ParticipantView join(@PathVariable long studyId, @Valid @RequestBody JoinRequest request) {
        return service.join(studyId, request.memberId());
    }

    @DeleteMapping("/studies/{studyId}/participants/{memberId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void leave(@PathVariable long studyId, @PathVariable long memberId) {
        service.leave(studyId, memberId);
    }

    @GetMapping("/studies/{studyId}/participants")
    List<ParticipantView> listParticipants(@PathVariable long studyId) {
        return service.listParticipants(studyId);
    }

    @PostMapping("/studies/{studyId}/meetings")
    @ResponseStatus(HttpStatus.CREATED)
    MeetingView createMeeting(@PathVariable long studyId, @Valid @RequestBody MeetingRequest request) {
        return service.createMeeting(studyId, request.memberId(), request.title(), request.location(), request.startsAt());
    }

    @GetMapping("/studies/{studyId}/meetings")
    List<MeetingView> listMeetings(@PathVariable long studyId) {
        return service.listMeetings(studyId);
    }

    record MemberRequest(@NotBlank @Size(max = 100) String name, @NotNull @Size(max = 1000) String bio) {}
    record StudyRequest(@Positive long leaderId, @NotBlank @Size(max = 120) String title,
                        @NotNull @Size(max = 1000) String description, @Positive int capacity) {}
    record JoinRequest(@Positive long memberId) {}
    record MeetingRequest(@Positive long memberId, @NotBlank @Size(max = 120) String title,
                          @NotBlank @Size(max = 200) String location, @NotNull @Future Instant startsAt) {}
}
