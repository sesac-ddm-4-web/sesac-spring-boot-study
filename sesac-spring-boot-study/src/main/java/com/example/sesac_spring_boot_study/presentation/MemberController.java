package com.example.sesac_spring_boot_study.presentation;

import com.example.sesac_spring_boot_study.application.MemberService;
import com.example.sesac_spring_boot_study.application.StudyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/members")
public class MemberController {
    private final MemberService memberService;
    private final StudyService studyService;

    public MemberController(MemberService memberService, StudyService studyService) {
        this.memberService = memberService;
        this.studyService = studyService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MemberDto.Response signUp(@Valid @RequestBody MemberDto.SignUpRequest request) {
        return memberService.signUp(request.email(), request.nickname());
    }

    @GetMapping("/{memberId}")
    public MemberDto.Response getMember(@PathVariable Long memberId) {
        return memberService.getMember(memberId);
    }

    @PutMapping("/{memberId}/profile")
    public MemberDto.Response updateProfile(@PathVariable Long memberId,
                                            @Valid @RequestBody MemberDto.ProfileUpdateRequest request) {
        return memberService.updateIntroduction(memberId, request.introduction());
    }

    @GetMapping("/{memberId}/studies")
    public List<StudyDto.JoinedStudy> getJoinedStudies(@PathVariable Long memberId) {
        return studyService.findJoinedStudies(memberId);
    }
}
