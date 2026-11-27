package com.example.studyservice.study.controller;

import com.example.studyservice.study.dto.res.MyStudyRes;
import com.example.studyservice.study.service.ParticipationService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class MyStudyController {

    private final ParticipationService participationService;

    public MyStudyController(ParticipationService participationService) {
        this.participationService = participationService;
    }

    @GetMapping("/members/me/studies")
    public List<MyStudyRes> findMyStudies(@AuthenticationPrincipal Long memberId) {
        return participationService.findMyStudies(memberId);
    }
}
