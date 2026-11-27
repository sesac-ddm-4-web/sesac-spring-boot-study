package com.example.sesac_spring_boot_study.presentation;

import com.example.sesac_spring_boot_study.application.StudyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/studies")
public class StudyController {
    private final StudyService studyService;

    public StudyController(StudyService studyService) {
        this.studyService = studyService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StudyDto.Detail create(@Valid @RequestBody StudyDto.CreateRequest request) {
        return studyService.create(request.leaderId(), request.title(),
                request.description(), request.maxCapacity());
    }

    @GetMapping
    public List<StudyDto.Summary> findAll() {
        return studyService.findAll();
    }

    @GetMapping("/{studyId}")
    public StudyDto.Detail findDetail(@PathVariable Long studyId) {
        return studyService.findDetail(studyId);
    }

    @PostMapping("/{studyId}/members")
    @ResponseStatus(HttpStatus.CREATED)
    public StudyDto.Participant join(@PathVariable Long studyId,
                                     @Valid @RequestBody StudyDto.JoinRequest request) {
        return studyService.join(studyId, request.memberId());
    }

    @DeleteMapping("/{studyId}/members/{memberId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void leave(@PathVariable Long studyId, @PathVariable Long memberId) {
        studyService.leave(studyId, memberId);
    }
}
